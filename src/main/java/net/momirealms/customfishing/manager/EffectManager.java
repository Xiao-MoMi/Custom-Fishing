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
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class EffectManager extends Function {

    private final CustomFishing plugin;
    private final HashMap<String, Item> baitItems;
    private final HashMap<String, Effect> baitEffects;
    private final HashMap<String, Item> rodItems;
    private final HashMap<String, Effect> rodEffects;
    private final HashMap<String, Item> utilItems;
    private final HashMap<String, Effect> utilEffects;
    private final HashMap<String, Effect> enchantEffects;

    public EffectManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.baitEffects = new HashMap<>();
        this.baitItems = new HashMap<>();
        this.rodEffects = new HashMap<>();
        this.rodItems = new HashMap<>();
        this.utilItems = new HashMap<>();
        this.enchantEffects = new HashMap<>();
        this.utilEffects = new HashMap<>();
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
        this.utilItems.clear();
        this.enchantEffects.clear();
        this.utilEffects.clear();
    }

    private void loadUtil() {
        File util_file = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + "utils");
        if (!util_file.exists()) {
            if (!util_file.mkdir()) return;
            plugin.saveResource("contents" + File.separator + "utils" + File.separator + "fish_finder.yml", false);
            plugin.saveResource("contents" + File.separator + "utils" + File.separator + "totem_items.yml", false);
            plugin.saveResource("contents" + File.separator + "utils" + File.separator + "splash_items.yml", false);
            plugin.saveResource("contents" + File.separator + "utils" + File.separator + "fisherman_talismans.yml", false);
        }
        File[] files = util_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection utilSection = config.getConfigurationSection(key);
                if (utilSection == null) continue;
                Item item = new Item(utilSection, key);
                item.setCfTag(new String[] {"util", key});
                utilItems.put(key, item);
                Effect effect = ConfigUtils.getEffect(utilSection.getConfigurationSection("effect"));
                if (utilSection.contains("requirements")) {
                    effect.setRequirements(ConfigUtils.getRequirements(utilSection.getConfigurationSection("requirements")));
                }
                utilEffects.put(key, effect);
            }
        }
        AdventureUtils.consoleMessage("[CustomFishing] Loaded <green>" + utilItems.size() + " <gray>util(s)");
    }

    private void loadEnchant() {
        File enchant_file = new File(plugin.getDataFolder(), "contents" + File.separator + "enchants");
        if (!enchant_file.exists()) {
            if (!enchant_file.mkdir()) return;
            plugin.saveResource("contents" + File.separator + "enchants" + File.separator + "default.yml", false);
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
                    Effect effect = ConfigUtils.getEffect(levelSection.getConfigurationSection(level + ".effect"));
                    if (levelSection.contains(level + ".requirements")) {
                        effect.setRequirements(ConfigUtils.getRequirements(levelSection.getConfigurationSection(level + ".requirements")));
                    }
                    enchantEffects.put((key.startsWith("eco") ? "minecraft" + key.substring(3) : key) + ":" + level, effect);
                }
                amount++;
            }
        }
        AdventureUtils.consoleMessage("[CustomFishing] Loaded <green>" + amount + " <gray>enchantment(s)");
    }

    private void loadBait() {
        File bait_file = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + "baits");
        if (!bait_file.exists()) {
            if (!bait_file.mkdir()) return;
            plugin.saveResource("contents" + File.separator + "baits" + File.separator + "default.yml", false);
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
                item.setCfTag(new String[] {"bait", key});
                baitItems.put(key, item);
                Effect effect = ConfigUtils.getEffect(baitSection.getConfigurationSection("effect"));
                if (baitSection.contains("requirements")) {
                    effect.setRequirements(ConfigUtils.getRequirementsWithMsg(baitSection.getConfigurationSection("requirements")));
                }
                baitEffects.put(key, effect);
            }
        }
        AdventureUtils.consoleMessage("[CustomFishing] Loaded <green>" + baitItems.size() + " <gray>bait(s)");
    }

    private void loadRod() {
        File rod_file = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + "rods");
        if (!rod_file.exists()) {
            if (!rod_file.mkdir()) return;
            plugin.saveResource("contents" + File.separator + "rods" + File.separator + "default.yml", false);
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
                rodSection.set("material", "fishing_rod");
                Item item = new Item(rodSection, key);
                item.setCfTag(new String[] {"rod", key});
                rodItems.put(key, item);
                Effect effect = ConfigUtils.getEffect(rodSection.getConfigurationSection("effect"));
                if (rodSection.contains("requirements")) {
                    effect.setRequirements(ConfigUtils.getRequirementsWithMsg(rodSection.getConfigurationSection("requirements")));
                }
                rodEffects.put(key, effect);
            }
        }
        AdventureUtils.consoleMessage("[CustomFishing] Loaded <green>" + rodItems.size() + " <gray>rod(s)");
    }

    @Nullable
    public Item getBaitItem(String key) {
        return baitItems.get(key);
    }

    @NotNull
    public HashMap<String, Item> getBaitItems() {
        return baitItems;
    }

    @Nullable
    public Effect getBaitEffect(String key) {
        return baitEffects.get(key);
    }

    @NotNull
    public HashMap<String, Effect> getBaitEffects() {
        return baitEffects;
    }

    @Nullable
    public Item getRodItem(String key) {
        return rodItems.get(key);
    }

    @NotNull
    public HashMap<String, Item> getRodItems() {
        return rodItems;
    }

    @Nullable
    public Effect getRodEffect(String key) {
        return rodEffects.get(key);
    }

    @NotNull
    public HashMap<String, Effect> getRodEffects() {
        return rodEffects;
    }

    @Nullable
    public Effect getEnchantEffect(String key) {
        return enchantEffects.get(key);
    }

    @NotNull
    public HashMap<String, Effect> getEnchantEffects() {
        return enchantEffects;
    }

    @Nullable
    public Item getUtilItem(String key) {
        return utilItems.get(key);
    }

    @NotNull
    public HashMap<String, Item> getUtilItems() {
        return utilItems;
    }

    @NotNull
    public HashMap<String, Effect> getUtilEffects() {
        return utilEffects;
    }

    @Nullable
    public Effect getUtilEffect(String key) {
        return utilEffects.get(key);
    }
}
