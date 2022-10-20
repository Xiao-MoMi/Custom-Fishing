package net.momirealms.customfishing.data.storage;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.data.PlayerBagData;
import net.momirealms.customfishing.util.ConfigUtil;
import net.momirealms.customfishing.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class FileStorageImpl implements DataStorageInterface {

    @Override
    public Inventory load(OfflinePlayer player) {
        YamlConfiguration config = ConfigUtil.readData(new File(CustomFishing.plugin.getDataFolder(), "fishingbag_data" + File.separator + player.getUniqueId() + ".yml"));
        String contents = config.getString("contents");
        int size = config.getInt("size", 9);
        ItemStack[] itemStacks = InventoryUtil.getInventoryItems(contents);
        Inventory inventory = Bukkit.createInventory(null, size, "{CustomFishing_Bag_" + player.getName() + "}");
        inventory.setContents(itemStacks);
        return inventory;
    }

    @Override
    public void save(PlayerBagData playerBagData) {
        YamlConfiguration data = new YamlConfiguration();
        Inventory inventory = playerBagData.getInventory();
        String contents = InventoryUtil.toBase64(inventory.getContents());
        data.set("contents", contents);
        data.set("size", inventory.getSize());
        try {
            data.save(new File(CustomFishing.plugin.getDataFolder(), "fishingbag_data" + File.separator + playerBagData.getPlayer().getUniqueId() + ".yml"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
