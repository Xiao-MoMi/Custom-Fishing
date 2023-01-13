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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.fishing.Bonus;
import net.momirealms.customfishing.object.loot.Item;
import net.momirealms.customfishing.object.loot.LeveledEnchantment;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ConfigUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class BonusManager extends Function {

    public static HashMap<String, ItemStack> BAITITEMS;
    public static HashMap<String, Bonus> BAIT;
    public static HashMap<String, ItemStack> RODITEMS;
    public static HashMap<String, Bonus> ROD;
    public static HashMap<String, Bonus> ENCHANTS = new HashMap<>();
    public static HashMap<String, ItemStack> UTILITEMS;

    @Override
    public void load() {
        BAIT = new HashMap<>();
        BAITITEMS = new HashMap<>();
        ROD = new HashMap<>();
        RODITEMS = new HashMap<>();
        ENCHANTS = new HashMap<>();
        loadRod();
        loadBait();
        loadEnchant();
        loadUtil();
    }

    @Override
    public void unload() {
        if (BAIT != null) BAIT.clear();
        if (BAITITEMS != null) BAITITEMS.clear();
        if (ROD != null) ROD.clear();
        if (RODITEMS != null) RODITEMS.clear();
        if (ENCHANTS != null) ENCHANTS.clear();
    }

    private void loadUtil() {
        UTILITEMS = new HashMap<>();
        File util_file = new File(CustomFishing.plugin.getDataFolder() + File.separator + "utils");
        if (!util_file.exists()) {
            if (!util_file.mkdir()) return;
            CustomFishing.plugin.saveResource("utils" + File.separator + "fishfinder.yml", false);
            CustomFishing.plugin.saveResource("utils" + File.separator + "totem_items.yml", false);
            CustomFishing.plugin.saveResource("utils" + File.separator + "splash_items.yml", false);
        }
        File[] files = util_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                Item item = new Item(Material.valueOf(config.getString(key + ".material", "PAPER").toUpperCase()), key);
                setItemProperties(config, key, item);
                UTILITEMS.put(key, ItemStackUtil.addIdentifier(ItemStackUtil.getFromItem(item), "util", key));
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + UTILITEMS.size() + " <gray>utils");
    }

    private void loadEnchant() {
        ENCHANTS = new HashMap<>();
        YamlConfiguration config = ConfigUtil.getConfig("enchant-bonus.yml");
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            config.getConfigurationSection(key).getKeys(false).forEach(level -> {
                Bonus bonus = new Bonus();
                config.getConfigurationSection(key + "." + level).getKeys(false).forEach(modifier -> {
                    switch (modifier) {
                        case "weight-add" -> {
                            HashMap<String, Integer> pm = new HashMap<>();
                            config.getConfigurationSection(key + "." + level + ".weight-add").getValues(false).forEach((group, value) -> {
                                pm.put(group, (Integer) value);
                            });
                            bonus.setWeightAS(pm);
                        }
                        case "weight-multiply" -> {
                            HashMap<String, Double> mq = new HashMap<>();
                            config.getConfigurationSection(key + "." + level + ".weight-multiply").getValues(false).forEach((group, value) -> {
                                mq.put(group, Double.parseDouble(String.valueOf(value))-1);
                            });
                            bonus.setWeightMD(mq);
                        }
                        case "time" -> bonus.setTime(config.getDouble(key + "." + level + ".time"));
                        case "difficulty" -> bonus.setDifficulty(config.getInt(key + "." + level + ".difficulty"));
                        case "double-loot" -> bonus.setDoubleLoot(config.getDouble(key + "." + level + ".double-loot"));
                        case "score" -> bonus.setScore(config.getDouble(key + "." + level + ".score"));
                        case "lava-fishing" -> bonus.setCanLavaFishing(config.getBoolean(key + "." + level + ".lava-fishing"));
                    }
                });
                ENCHANTS.put(key + ":" + level, bonus);
            });
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + keys.size() + " <gray>enchantments");
    }

    private void loadBait() {
        BAITITEMS = new HashMap<>();
        BAIT = new HashMap<>();
        File bait_file = new File(CustomFishing.plugin.getDataFolder() + File.separator + "baits");
        if (!bait_file.exists()) {
            if (!bait_file.mkdir()) return;
            CustomFishing.plugin.saveResource("baits" + File.separator + "default.yml", false);
        }
        File[] files = bait_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                Item item = new Item(Material.valueOf(config.getString(key + ".material", "PAPER").toUpperCase()), key);
                setItemProperties(config, key, item);
                BAITITEMS.put(key, ItemStackUtil.addIdentifier(ItemStackUtil.getFromItem(item), "bait", key));
                if (config.contains(key + ".modifier")) {
                    BAIT.put(key, getBonus(config, key));
                }
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + BAITITEMS.size() + " <gray>baits");
    }

    public static Bonus getBonus(YamlConfiguration config, String key) {
        Bonus bonus = new Bonus();
        config.getConfigurationSection(key + ".modifier").getKeys(false).forEach(modifier -> {
            switch (modifier) {
                case "weight-add" -> {
                    HashMap<String, Integer> as = new HashMap<>();
                    config.getConfigurationSection(key + ".modifier.weight-add").getValues(false).forEach((group, value) -> {
                        as.put(group, (Integer) value);
                    });
                    bonus.setWeightAS(as);
                }
                case "weight-multiply" -> {
                    HashMap<String, Double> md = new HashMap<>();
                    config.getConfigurationSection(key + ".modifier.weight-multiply").getValues(false).forEach((group, value) -> {
                        md.put(group, Double.parseDouble(String.valueOf(value))-1);
                    });
                    bonus.setWeightMD(md);
                }
                case "time" -> bonus.setTime(config.getDouble(key + ".modifier.time"));
                case "difficulty" -> bonus.setDifficulty(config.getInt(key + ".modifier.difficulty"));
                case "double-loot" -> bonus.setDoubleLoot(config.getDouble(key + ".modifier.double-loot"));
                case "score" -> bonus.setScore(config.getDouble(key + ".modifier.score"));
                case "lava-fishing" -> bonus.setCanLavaFishing(config.getBoolean(key + ".modifier.lava-fishing", false));
            }
        });
        return bonus;
    }

    private void loadRod() {
        ROD = new HashMap<>();
        RODITEMS = new HashMap<>();
        File rod_file = new File(CustomFishing.plugin.getDataFolder() + File.separator + "rods");
        if (!rod_file.exists()) {
            if (!rod_file.mkdir()) return;
            CustomFishing.plugin.saveResource("rods" + File.separator + "default.yml", false);
        }
        File[] files = rod_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                Item item = new Item(Material.FISHING_ROD, key);
                setItemProperties(config, key, item);
                RODITEMS.put(key, ItemStackUtil.addIdentifier(ItemStackUtil.getFromItem(item), "rod", key));
                if (config.contains(key + ".modifier")) {
                    ROD.put(key, getBonus(config, key));
                }
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + RODITEMS.size() + " <gray>rods");
    }

    public static void setItemProperties(ConfigurationSection config, String key, Item item) {
        item.setUnbreakable(config.getBoolean(key + ".unbreakable", false));
        if (config.contains(key + ".display.lore")) item.setLore(config.getStringList(key + ".display.lore"));
        if (config.contains(key + ".display.name")) item.setName(config.getString(key + ".display.name"));
        if (config.contains(key + ".custom-model-data")) item.setCustomModelData(config.getInt(key + ".custom-model-data"));
        if (config.contains(key + ".enchantments")) {
            List<LeveledEnchantment> enchantmentList = new ArrayList<>();
            config.getConfigurationSection(key + ".enchantments").getKeys(false).forEach(enchant -> {
                LeveledEnchantment leveledEnchantment = new LeveledEnchantment(
                        NamespacedKey.fromString(enchant),
                        config.getInt(key + ".enchantments." + enchant)
                );
                enchantmentList.add(leveledEnchantment);
            });
            item.setEnchantment(enchantmentList);
        }
        if (config.contains(key + ".item_flags")) {
            ArrayList<ItemFlag> itemFlags = new ArrayList<>();
            config.getStringList(key + ".item_flags").forEach(flag -> itemFlags.add(ItemFlag.valueOf(flag)));
            item.setItemFlags(itemFlags);
        }
        if (config.contains(key + ".nbt")) {
            Map<String, Object> nbt = config.getConfigurationSection(key + ".nbt").getValues(false);
            item.setNbt(nbt);
        }
        if (config.contains(key + ".head64")) {
            item.setHead64(config.getString(key + ".head64"));
        }
    }
}
