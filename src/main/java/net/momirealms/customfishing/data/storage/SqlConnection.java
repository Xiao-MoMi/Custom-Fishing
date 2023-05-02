/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.data.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlConnection {

    private boolean secondTry = false;
    private boolean firstTry = false;
    private HikariDataSource hikariDataSource;
    private String tablePrefix;
    private final MySQLStorageImpl mySQLStorage;

    public SqlConnection(MySQLStorageImpl mySQLStorage) {
        this.mySQLStorage = mySQLStorage;
    }

    public void createNewHikariConfiguration() {
        ConfigUtils.update("database.yml");
        YamlConfiguration config = ConfigUtils.getConfig("database.yml");
        String storageMode = config.getString("data-storage-method", "MySQL");
        HikariConfig hikariConfig = new HikariConfig();
        String sql = "mysql";
        if (storageMode.equalsIgnoreCase("MariaDB")) {
            hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
            sql = "mariadb";
        }
        tablePrefix = config.getString(storageMode + ".table-prefix");
        hikariConfig.setPoolName("[CustomFishing]");
        hikariConfig.setJdbcUrl("jdbc:" + sql + "://" + config.getString(storageMode + ".host") + ":" + config.getString(storageMode + ".port") + "/" + config.getString(storageMode + ".database"));
        hikariConfig.setUsername(config.getString(storageMode + ".user"));
        hikariConfig.setPassword(config.getString(storageMode + ".password"));
        hikariConfig.setMaximumPoolSize(config.getInt(storageMode + ".Pool-Settings.maximum-pool-size"));
        hikariConfig.setMinimumIdle(config.getInt(storageMode + ".Pool-Settings.minimum-idle"));
        hikariConfig.setMaxLifetime(config.getInt(storageMode + ".Pool-Settings.maximum-lifetime"));
        hikariConfig.setConnectionTimeout(3000);
        hikariConfig.setIdleTimeout(hikariConfig.getMinimumIdle() < hikariConfig.getMaximumPoolSize() ? config.getInt(storageMode + ".Pool-Settings.idle-timeout") : 0);
        ConfigurationSection section = config.getConfigurationSection(storageMode + ".properties");
        if (section != null) {
            for (String property : section.getKeys(false)) {
                hikariConfig.addDataSourceProperty(property, config.getString(storageMode + ".properties." + property));
            }
        }
        try {
            hikariDataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to create sql connection");
        }

        if (config.getBoolean("migration", false)) {
            mySQLStorage.migrate();
            config.set("migration", false);
            try {
                config.save(new File(CustomFishing.getInstance().getDataFolder(), "database.yml"));
            }
            catch (IOException e) {
                AdventureUtils.consoleMessage("<RED>[CustomFishing] Error occurred when saving database config");
            }
        }
    }

    public boolean setGlobalConnection() {
        try {
            createNewHikariConfiguration();
            Connection connection = getConnection();
            connection.close();
            if (secondTry) {
                AdventureUtils.consoleMessage("[CustomFishing] Successfully reconnect to SQL!");
            } else {
                secondTry = true;
            }
            return true;
        } catch (SQLException e) {
            AdventureUtils.consoleMessage("<red>[CustomFishing] Error! Failed to connect to SQL!</red>");
            e.printStackTrace();
            close();
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    public boolean canConnect() {
        if (hikariDataSource == null) {
            return setGlobalConnection();
        }
        if (hikariDataSource.isClosed()) {
            return setGlobalConnection();
        }
        return true;
    }

    public void close() {
        if (hikariDataSource != null && hikariDataSource.isRunning()) {
            hikariDataSource.close();
        }
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Connection getConnectionAndCheck() {
        if (!canConnect()) {
            return null;
        }
        try {
            return getConnection();
        } catch (SQLException e) {
            if (firstTry) {
                firstTry = false;
                close();
                return getConnectionAndCheck();
            } else {
                firstTry = true;
                AdventureUtils.consoleMessage("<red>[CustomFishing] Error! Failed to connect to SQL!</red>");
                close();
                e.printStackTrace();
                return null;
            }
        }
    }
}