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
    public void initialize() {

    }

    @Override
    public void disable() {

    }

    @Override
    public Inventory loadBagData(OfflinePlayer player) {
        YamlConfiguration config = ConfigUtil.readData(new File(CustomFishing.plugin.getDataFolder(), "fishingbag_data" + File.separator + player.getUniqueId() + ".yml"));
        String contents = config.getString("contents");
        int size = config.getInt("size", 9);
        ItemStack[] itemStacks = InventoryUtil.getInventoryItems(contents);
        Inventory inventory = Bukkit.createInventory(null, size, "{CustomFishing_Bag_" + player.getName() + "}");
        if (itemStacks != null) inventory.setContents(itemStacks);
        return inventory;
    }

    @Override
    public void saveBagData(PlayerBagData playerBagData) {
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
