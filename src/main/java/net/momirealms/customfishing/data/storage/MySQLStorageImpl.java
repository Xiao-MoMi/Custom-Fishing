package net.momirealms.customfishing.data.storage;

import net.momirealms.customfishing.data.PlayerBagData;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

public class MySQLStorageImpl implements DataStorageInterface {

    @Override
    public Inventory load(OfflinePlayer player) {
        return null;
    }

    @Override
    public void save(PlayerBagData playerBagData) {

    }
}
