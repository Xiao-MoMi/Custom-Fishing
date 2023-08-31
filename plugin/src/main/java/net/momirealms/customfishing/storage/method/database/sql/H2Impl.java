package net.momirealms.customfishing.storage.method.database.sql;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.StorageType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.h2.jdbcx.JdbcConnectionPool;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class H2Impl extends AbstractSQLDatabase {

    private JdbcConnectionPool connectionPool;

    public H2Impl(CustomFishingPlugin plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        File databaseFile = new File(plugin.getDataFolder(), config.getString("H2.file", "data.db"));
        super.tablePrefix = config.getString("H2.table-prefix", "customfishing");

        final String url = String.format("jdbc:h2:%s", databaseFile.getAbsolutePath());
        this.connectionPool = JdbcConnectionPool.create(url, "sa", "");
        super.createTableIfNotExist();
    }

    @Override
    public void disable() {
        if (connectionPool != null) {
            connectionPool.dispose();
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.H2;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }
}
