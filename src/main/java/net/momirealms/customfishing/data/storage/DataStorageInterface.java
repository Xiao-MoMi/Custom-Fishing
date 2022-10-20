package net.momirealms.customfishing.data.storage;

import net.momirealms.customfishing.data.PlayerBagData;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

public interface DataStorageInterface {

    Inventory load(OfflinePlayer player);

    void save(PlayerBagData playerBagData);

}
