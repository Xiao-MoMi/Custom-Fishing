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

package net.momirealms.customfishing.storage.method.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.*;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * An abstract base class for SQL databases using the HikariCP connection pool, which handles player data storage.
 */
public abstract class AbstractHikariDatabase extends AbstractSQLDatabase implements LegacyDataStorageInterface {

    private HikariDataSource dataSource;
    private final String driverClass;
    private final String sqlBrand;

    public AbstractHikariDatabase(CustomFishingPlugin plugin) {
        super(plugin);
        this.driverClass = getStorageType() == StorageType.MariaDB ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
        this.sqlBrand = getStorageType() == StorageType.MariaDB ? "MariaDB" : "MySQL";
        try {
            Class.forName(this.driverClass);
        } catch (ClassNotFoundException e1) {
            if (getStorageType() == StorageType.MariaDB) {
                LogUtils.warn("No MariaDB driver is found");
            } else if (getStorageType() == StorageType.MySQL) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException e2) {
                    LogUtils.warn("No MySQL driver is found");
                }
            }
        }
    }

    /**
     * Initialize the database connection pool and create tables if they don't exist.
     */
    @Override
    public void initialize() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        ConfigurationSection section = config.getConfigurationSection(sqlBrand);

        if (section == null) {
            LogUtils.warn("Failed to load database config. It seems that your config is broken. Please regenerate a new one.");
            return;
        }

        super.tablePrefix = section.getString("table-prefix", "customfishing");
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(section.getString("user", "root"));
        hikariConfig.setPassword(section.getString("password", "pa55w0rd"));
        hikariConfig.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s%s",
                sqlBrand.toLowerCase(Locale.ENGLISH),
                section.getString("host", "localhost"),
                section.getString("port", "3306"),
                section.getString("database", "minecraft"),
                section.getString("connection-parameters")
        ));
        hikariConfig.setDriverClassName(driverClass);
        hikariConfig.setMaximumPoolSize(section.getInt("Pool-Settings.max-pool-size", 10));
        hikariConfig.setMinimumIdle(section.getInt("Pool-Settings.min-idle", 10));
        hikariConfig.setMaxLifetime(section.getLong("Pool-Settings.max-lifetime", 180000L));
        hikariConfig.setConnectionTimeout(section.getLong("Pool-Settings.time-out", 20000L));
        hikariConfig.setPoolName("CustomFishingHikariPool");
        try {
            hikariConfig.setKeepaliveTime(section.getLong("Pool-Settings.keep-alive-time", 60000L));
        } catch (NoSuchMethodError ignored) {
        }

        final Properties properties = new Properties();
        properties.putAll(
                Map.of("cachePrepStmts", "true",
                        "prepStmtCacheSize", "250",
                        "prepStmtCacheSqlLimit", "2048",
                        "useServerPrepStmts", "true",
                        "useLocalSessionState", "true",
                        "useLocalTransactionState", "true"
                ));
        properties.putAll(
                Map.of(
                        "rewriteBatchedStatements", "true",
                        "cacheResultSetMetadata", "true",
                        "cacheServerConfiguration", "true",
                        "elideSetAutoCommits", "true",
                        "maintainTimeStats", "false")
        );
        hikariConfig.setDataSourceProperties(properties);
        dataSource = new HikariDataSource(hikariConfig);
        super.createTableIfNotExist();
    }

    /**
     * Disable the database by closing the connection pool.
     */
    @Override
    public void disable() {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }

    /**
     * Get a connection to the SQL database from the connection pool.
     *
     * @return A database connection.
     * @throws SQLException If there is an error establishing a connection.
     */
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Retrieve legacy player data from the SQL database.
     *
     * @param uuid The UUID of the player.
     * @return A CompletableFuture containing the optional legacy player data.
     */
    @Override
    public CompletableFuture<Optional<PlayerData>> getLegacyPlayerData(UUID uuid) {
        var future = new CompletableFuture<Optional<PlayerData>>();
        plugin.getScheduler().runTaskAsync(() -> {
            try (
                Connection connection = getConnection()
            ) {
                var builder = new PlayerData.Builder().setName("");
                PreparedStatement statementOne = connection.prepareStatement(String.format(SqlConstants.SQL_SELECT_BY_UUID, getTableName("fishingbag")));
                statementOne.setString(1, uuid.toString());
                ResultSet rsOne = statementOne.executeQuery();
                if (rsOne.next()) {
                    int size = rsOne.getInt("size");
                    String contents = rsOne.getString("contents");
                    builder.setBagData(new InventoryData(contents, size));
                } else {
                    builder.setBagData(InventoryData.empty());
                }

                PreparedStatement statementTwo = connection.prepareStatement(String.format(SqlConstants.SQL_SELECT_BY_UUID, getTableName("selldata")));
                statementTwo.setString(1, uuid.toString());
                ResultSet rsTwo = statementTwo.executeQuery();
                if (rsTwo.next()) {
                    int date = rsTwo.getInt("date");
                    double money = rsTwo.getInt("money");
                    builder.setEarningData(new EarningData(money, date));
                } else {
                    builder.setEarningData(EarningData.empty());
                }

                PreparedStatement statementThree = connection.prepareStatement(String.format(SqlConstants.SQL_SELECT_BY_UUID, getTableName("statistics")));
                statementThree.setString(1, uuid.toString());
                ResultSet rsThree = statementThree.executeQuery();
                if (rsThree.next()) {
                    String stats = rsThree.getString("stats");
                    var amountMap = (Map<String, Integer>) Arrays.stream(stats.split(";"))
                            .map(element -> element.split(":"))
                            .filter(pair -> pair.length == 2)
                            .collect(Collectors.toMap(pair -> pair[0], pair -> Integer.parseInt(pair[1])));
                    builder.setStats(new StatisticData(amountMap, new HashMap<>()));
                } else {
                    builder.setStats(StatisticData.empty());
                }
                future.complete(Optional.of(builder.build()));
            } catch (SQLException e) {
                LogUtils.warn("Failed to get " + uuid + "'s data.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
