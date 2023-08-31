package net.momirealms.customfishing.storage.method.database.sql;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.StorageType;

public class MySQLImpl extends AbstractHikariDatabase {

    public MySQLImpl(CustomFishingPlugin plugin) {
        super(plugin);
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MySQL;
    }
}
