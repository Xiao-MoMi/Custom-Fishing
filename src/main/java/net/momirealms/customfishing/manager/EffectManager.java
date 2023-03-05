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
import net.momirealms.customfishing.fishing.Effect;
import net.momirealms.customfishing.fishing.loot.Item;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class EffectManager extends Function {

    private final CustomFishing plugin;
    private final HashMap<String, ItemStack> baitItems;
    private final HashMap<String, Effect> baitEffects;
    private final HashMap<String, ItemStack> rodItems;
    private final HashMap<String, Effect> rodEffects;
    private final HashMap<String, Effect> enchantEffects;
    private final HashMap<String, ItemStack> utilItems;

    public EffectManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.baitEffects = new HashMap<>();
        this.baitItems = new HashMap<>();
        this.rodEffects = new HashMap<>();
        this.rodItems = new HashMap<>();
        this.utilItems = new HashMap<>();
        this.enchantEffects = new HashMap<>();
    }

    @Override
    public void load() {
        loadRod();
        loadBait();
        loadEnchant();
        loadUtil();
    }

    @Override
    public void unload() {
        this.baitEffects.clear();
        this.baitItems.clear();
        this.rodEffects.clear();
        this.rodItems.clear();
        this.enchantEffects.clear();
    }

    private void loadUtil() {
        File util_file = new File(plugin.getDataFolder() + File.separator + "utils");
        if (!util_file.exists()) {
            if (!util_file.mkdir()) return;
            plugin.saveResource("utils" + File.separator + "fish_finder.yml", false);
            plugin.saveResource("utils" + File.separator + "totem_items.yml", false);
            plugin.saveResource("utils" + File.separator + "splash_items.yml", false);
        }
        File[] files = util_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection itemSection = config.getConfigurationSection(key);
                if (itemSection == null) continue;
                Item item = new Item(itemSection, key);
                utilItems.put(key, ItemStackUtil.addIdentifier(ItemStackUtil.getFromItem(item), "util", key));
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + utilItems.size() + " <gray>util(s)");
    }

    private void loadEnchant() {
        File enchant_file = new File(plugin.getDataFolder() + File.separator + "enchants");
        if (!enchant_file.exists()) {
            if (!enchant_file.mkdir()) return;
            plugin.saveResource("enchants" + File.separator + "default.yml", false);
        }
        File[] files = enchant_file.listFiles();
        if (files == null) return;
        int amount = 0;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {
                ConfigurationSection levelSection = config.getConfigurationSection(key);
                if (levelSection == null) continue;
                for (String level : levelSection.getKeys(false)) {
                    enchantEffects.put(key + ":" + level, getEffect(levelSection.getConfigurationSection(key)));
                }
                amount++;
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + amount + " <gray>enchantment(s)");
    }

    private void loadBait() {
        File bait_file = new File(plugin.getDataFolder() + File.separator + "baits");
        if (!bait_file.exists()) {
            if (!bait_file.mkdir()) return;
            plugin.saveResource("baits" + File.separator + "default.yml", false);
        }
        File[] files = bait_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection baitSection = config.getConfigurationSection(key);
                if (baitSection == null) continue;
                Item item = new Item(baitSection, key);
                baitItems.put(key, ItemStackUtil.addIdentifier(ItemStackUtil.getFromItem(item), "bait", key));
                if (baitSection.contains("effect")) {
                    baitEffects.put(key, getEffect(baitSection.getConfigurationSection("effect")));
                }
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + baitItems.size() + " <gray>bait(s)");
    }

    private void loadRod() {
        File rod_file = new File(plugin.getDataFolder() + File.separator + "rods");
        if (!rod_file.exists()) {
            if (!rod_file.mkdir()) return;
            plugin.saveResource("rods" + File.separator + "default.yml", false);
        }
        File[] files = rod_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection rodSection = config.getConfigurationSection(key);
                if (rodSection == null) continue;
                Item item = new Item(rodSection, key);
                rodItems.put(key, ItemStackUtil.addIdentifier(ItemStackUtil.getFromItem(item), "rod", key));
                if (rodSection.contains("effect")) {
                    rodEffects.put(key, getEffect(rodSection.getConfigurationSection("effect")));
                }
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + rodItems.size() + " <gray>rod(s)");
    }

    public static Effect getEffect(ConfigurationSection section) {
        Effect effect = new Effect();
        if (section == null) return effect;
        for (String modifier : section.getKeys(false)) {
            switch (modifier) {
                case "weight-add" -> {
                    HashMap<String, Integer> as = new HashMap<>();
                    Objects.requireNonNull(section.getConfigurationSection(modifier)).getValues(false).forEach((group, value) -> as.put(group, (Integer) value));
                    effect.setWeightAS(as);
                }
                case "weight-multiply" -> {
                    HashMap<String, Double> md = new HashMap<>();
                    Objects.requireNonNull(section.getConfigurationSection(modifier)).getValues(false).forEach((group, value) -> md.put(group, Double.parseDouble(String.valueOf(value))-1));
                    effect.setWeightMD(md);
                }
                case "time" -> effect.setTimeModifier(section.getDouble(modifier));
                case "difficulty" -> effect.setDifficulty(section.getInt(modifier));
                case "double-loot" -> effect.setDoubleLootChance(section.getDouble(modifier));
                case "score" -> effect.setScoreMultiplier(section.getDouble(modifier) - 1);
                case "size-multiply" -> effect.setSizeMultiplier(section.getDouble(modifier) - 1);
                case "lava-fishing" -> effect.setCanLavaFishing(section.getBoolean(modifier, false));
            }
        }
        return effect;
    }

    @Nullable
    public ItemStack getBaitItem(String key) {
        return baitItems.get(key);
    }

    public HashMap<String, ItemStack> getBaitItems() {
        return baitItems;
    }

    @Nullable
    public Effect getBaitEffect(String key) {
        return baitEffects.get(key);
    }

    public HashMap<String, Effect> getBaitEffects() {
        return baitEffects;
    }

    @Nullable
    public ItemStack getRodItem(String key) {
        return rodItems.get(key);
    }

    public HashMap<String, ItemStack> getRodItems() {
        return rodItems;
    }

    @Nullable
    public Effect getRodEffect(String key) {
        return rodEffects.get(key);
    }

    public HashMap<String, Effect> getRodEffects() {
        return rodEffects;
    }

    @Nullable
    public Effect getEnchantEffect(String key) {
        return enchantEffects.get(key);
    }

    public HashMap<String, Effect> getEnchantEffects() {
        return enchantEffects;
    }

    @Nullable
    public ItemStack getUtilItem(String key) {
        return utilItems.get(key);
    }

    public HashMap<String, ItemStack> getUtilItems() {
        return utilItems;
    }
}
