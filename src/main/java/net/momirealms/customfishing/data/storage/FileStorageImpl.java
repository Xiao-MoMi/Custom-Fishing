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
import net.momirealms.customfishing.data.PlayerSellData;
import net.momirealms.customfishing.util.ConfigUtil;
import net.momirealms.customfishing.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileStorageImpl implements DataStorageInterface {

    private final CustomFishing plugin;

    public FileStorageImpl(CustomFishing plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void disable() {

    }

    @Override
    public Inventory loadBagData(UUID uuid, boolean force) {
        YamlConfiguration config = ConfigUtil.readData(new File(plugin.getDataFolder(), "fishingbag_data" + File.separator + uuid + ".yml"));
        String contents = config.getString("contents");
        int size = config.getInt("size", 9);
        ItemStack[] itemStacks = InventoryUtil.getInventoryItems(contents);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        Inventory inventory = Bukkit.createInventory(null, size, "{CustomFishing_Bag_" + offlinePlayer.getName() + "}");
        if (itemStacks != null) inventory.setContents(itemStacks);
        return inventory;
    }

    @Override
    public void saveBagData(UUID uuid, Inventory inventory, boolean unlock) {
        YamlConfiguration data = new YamlConfiguration();
        String contents = InventoryUtil.toBase64(inventory.getContents());
        data.set("contents", contents);
        data.set("size", inventory.getSize());
        try {
            data.save(new File(plugin.getDataFolder(), "fishingbag_data" + File.separator + uuid + ".yml"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerSellData loadSellData(UUID uuid, boolean force) {
        YamlConfiguration data = ConfigUtil.readData(new File(plugin.getDataFolder(), "sell_data" + File.separator + uuid + ".yml"));
        int date = data.getInt("date");
        double money = data.getDouble("earnings");
        return new PlayerSellData(money, date);
    }

    @Override
    public void saveSellData(UUID uuid, PlayerSellData playerSellData, boolean unlock) {
        YamlConfiguration data = new YamlConfiguration();
        data.set("date", playerSellData.getDate());
        data.set("earnings", playerSellData.getMoney());
        try {
            data.save(new File(plugin.getDataFolder(), "sell_data" + File.separator + uuid + ".yml"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.YAML;
    }
}
