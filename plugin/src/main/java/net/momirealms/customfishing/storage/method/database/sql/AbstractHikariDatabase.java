package net.momirealms.customfishing.storage.method.database.sql;

import com.zaxxer.hikari.HikariDataSource;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.StorageType;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractHikariDatabase extends AbstractSQLDatabase {

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
                    LogUtils.warn("It seems that you are not using MySQL 8.0+. It's recommended to update.");
                } catch (ClassNotFoundException e2) {
                    LogUtils.warn("No MySQL driver is found");
                }
            }
        }
    }

    @Override
    public void initialize() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        ConfigurationSection section = config.getConfigurationSection(sqlBrand);
        if (section == null) {
            LogUtils.warn("Failed to load database config. It seems that your config is broken. Please regenerate a new one.");
            return;
        }

        super.tablePrefix = section.getString("table-prefix", "customfishing");
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s%s",
                sqlBrand.toLowerCase(Locale.ENGLISH),
                section.getString("host", "localhost"),
                section.getString("port", "3306"),
                section.getString("database", "minecraft"),
                section.getString("connection-parameters")
        ));

        dataSource.setUsername(section.getString("user", "root"));
        dataSource.setPassword(section.getString("password", "pa55w0rd"));

        dataSource.setMaximumPoolSize(section.getInt("Pool-Settings.max-pool-size", 10));
        dataSource.setMinimumIdle(section.getInt("Pool-Settings.min-idle", 10));
        dataSource.setMaxLifetime(section.getLong("Pool-Settings.max-lifetime", 180000L));
        dataSource.setKeepaliveTime(section.getLong("Pool-Settings.keep-alive-time", 60000L));
        dataSource.setConnectionTimeout(section.getLong("Pool-Settings.time-out", 20000L));
        dataSource.setPoolName("CustomFishingHikariPool");

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
        dataSource.setDataSourceProperties(properties);
        super.createTableIfNotExist();
    }

    @Override
    public void disable() {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
