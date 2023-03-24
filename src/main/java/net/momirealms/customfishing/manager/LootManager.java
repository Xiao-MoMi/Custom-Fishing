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
import net.momirealms.customfishing.fishing.MiniGameConfig;
import net.momirealms.customfishing.fishing.action.Action;
import net.momirealms.customfishing.fishing.loot.*;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.LeveledEnchantment;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import net.momirealms.customfishing.util.ItemStackUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class LootManager extends Function {

    private final CustomFishing plugin;
    private final HashMap<String, Loot> waterLoots;
    private final HashMap<String, Loot> lavaLoots;
    private final HashMap<String, Item> lootItems;
    private final HashMap<String, List<String>> category;
    private Loot vanilla_loot;

    public LootManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.waterLoots = new HashMap<>();
        this.lavaLoots = new HashMap<>();
        this.lootItems = new HashMap<>();
        this.category = new HashMap<>();
    }

    @Nullable
    public ItemStack build(String key) {
        Item item = lootItems.get(key);
        return item == null || item.getMaterial() == Material.AIR ? new ItemStack(Material.AIR) : ItemStackUtils.getFromItem(item);
    }

    @Override
    public void load() {
        this.vanilla_loot = new Loot(
                "vanilla",
                "vanilla",
                null,
                0,
                false,
                0,
                false,
                false
        );
        this.loadItems();
        this.loadMobs();
        this.loadCategories();
        AdventureUtils.consoleMessage("[CustomFishing] Loaded <green>" + (this.lavaLoots.size() + this.waterLoots.size()) + " <gray>loot(s)");
        AdventureUtils.consoleMessage("[CustomFishing] Loaded <green>" + (this.category.size()) + " <gray>category(s)");
    }

    @Override
    public void unload() {
        this.waterLoots.clear();
        this.lavaLoots.clear();
        this.lootItems.clear();
        this.category.clear();
        this.vanilla_loot = null;
    }

    @Nullable
    public Loot getLoot(String key) {
        Loot loot = this.waterLoots.get(key);
        if (loot == null) {
            loot = this.lavaLoots.get(key);
        }
        return loot;
    }

    public boolean hasLoot(String key) {
        boolean has = this.waterLoots.containsKey(key);
        if (!has) {
            has = this.lavaLoots.containsKey(key);
        }
        return has;
    }

    private void loadCategories() {
        File category_file = new File(plugin.getDataFolder() + File.separator + "categories");
        if (!category_file.exists()) {
            if (!category_file.mkdir()) return;
            plugin.saveResource("categories" + File.separator + "default.yml", false);
        }
        File[] files = category_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            outer:
                for (String key : config.getKeys(false)) {
                    List<String> fishIDs = config.getStringList(key);
                    for (String id : fishIDs) {
                        if (!waterLoots.containsKey(id) && !lavaLoots.containsKey(id)) {
                            AdventureUtils.consoleMessage("<red>[CustomFishing] Fish ID " + id + " doesn't exist in category " + key);
                            continue outer;
                        }
                    }
                    category.put(key, fishIDs);
                }
        }
    }

    private void loadMobs() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) return;
        File mob_file = new File(plugin.getDataFolder() + File.separator + "mobs");
        if (!mob_file.exists()) {
            if (!mob_file.mkdir()) return;
            plugin.saveResource("mobs" + File.separator + "default.yml", false);
        }
        File[] files = mob_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection mobSection = config.getConfigurationSection(key);
                if (mobSection == null) continue;
                if (!mobSection.getBoolean("enable", true)) continue;
                Mob loot = new Mob(
                        key,
                        mobSection.contains("nick") ? mobSection.getString("nick") : AdventureUtils.replaceLegacy(mobSection.getString("mobID", key)),
                        getMiniGames(mobSection),
                        mobSection.getInt("weight",10),
                        mobSection.getBoolean("show-in-fishfinder", true),
                        mobSection.getDouble("score",10d),
                        mobSection.getString(key + ".mobID", key),
                        mobSection.getInt(key + ".level", 0),
                        new MobVector(
                                mobSection.getDouble("vector.horizontal",1.1),
                                mobSection.getDouble("vector.vertical",1.3)
                        ),
                        mobSection.getBoolean("disable-bar-mechanic", false),
                        mobSection.getBoolean("disable-stats", false)
                );

                setActions(mobSection, loot);
                loot.setRequirements(ConfigUtils.getRequirements(mobSection.getConfigurationSection("requirements")));

                if (mobSection.getBoolean("in-lava", false)) lavaLoots.put(key, loot);
                else waterLoots.put(key, loot);
            }
        }
    }

    private void loadItems() {
        File loot_file = new File(plugin.getDataFolder() + File.separator + "loots");
        if (!loot_file.exists()) {
            if (!loot_file.mkdir()) return;
            plugin.saveResource("loots" + File.separator + "default.yml", false);
            plugin.saveResource("loots" + File.separator + "example.yml", false);
        }
        File[] files = loot_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection lootSection = config.getConfigurationSection(key);
                if (lootSection == null) continue;
                if (!lootSection.getBoolean("enable", true)) continue;
                String material = lootSection.getString("material","COD");
                DroppedItem loot = new DroppedItem(
                        key,
                        lootSection.contains("nick") ? lootSection.getString("nick") : AdventureUtils.replaceLegacy(lootSection.getString("display.name", key)),
                        material.contains(":") ? material : key,
                        getMiniGames(lootSection),
                        lootSection.getInt("weight",10),
                        lootSection.getBoolean("show-in-fishfinder", true),
                        lootSection.getDouble("score"),
                        lootSection.getBoolean("random-durability", false),
                        lootSection.getBoolean("disable-bar-mechanic", false),
                        lootSection.getBoolean("disable-stats", false)
                );

                if (lootSection.contains("size")) {
                    String[] size = StringUtils.split(lootSection.getString("size", "1~10"), "~");
                    if (size.length != 2) {
                        AdventureUtils.consoleMessage("<red>[CustomFishing] Wrong size found at " + key);
                        continue;
                    }
                    loot.setSize(size);
                }
                if (lootSection.contains("price")) {
                    loot.setBasicPrice((float) lootSection.getDouble("price.base", 0));
                    loot.setSizeBonus((float) lootSection.getDouble("price.bonus", 0));
                }
                if (lootSection.contains("random-enchantments")) {
                    List<LeveledEnchantment> randomEnchants = new ArrayList<>();
                    ConfigurationSection enchantSection = lootSection.getConfigurationSection("random-enchantments");
                    if (enchantSection != null) {
                        for (Map.Entry<String, Object> entry : enchantSection.getValues(false).entrySet()) {
                            if (entry.getValue() instanceof MemorySection memorySection){
                                LeveledEnchantment enchantment = new LeveledEnchantment(
                                        NamespacedKey.fromString(memorySection.getString("enchant", "minecraft:sharpness")),
                                        memorySection.getInt("level"),
                                        memorySection.getDouble("chance")
                                );
                                randomEnchants.add(enchantment);
                            }
                        }
                    }
                    loot.setRandomEnchants(randomEnchants.toArray(new LeveledEnchantment[0]));
                }

                setActions(lootSection, loot);
                loot.setRequirements(ConfigUtils.getRequirements(lootSection.getConfigurationSection("requirements")));
                if (key.equals("vanilla")) {
                    vanilla_loot = loot;
                    continue;
                }
                if (lootSection.getBoolean("in-lava", false)) lavaLoots.put(key, loot);
                else waterLoots.put(key, loot);
                //not a CustomFishing loot
                if (material.contains(":")) continue;
                Item item = new Item(lootSection, key);
                if (ConfigManager.addTagToFish) item.setCfTag(new String[] {"loot", key});
                lootItems.put(key, item);
            }
        }
    }

    private void setActions(ConfigurationSection section, Loot loot) {
        loot.setSuccessActions(ConfigUtils.getActions(section.getConfigurationSection("action.success"), loot.getNick()));
        loot.setFailureActions(ConfigUtils.getActions(section.getConfigurationSection("action.failure"), loot.getNick()));
        loot.setHookActions(ConfigUtils.getActions(section.getConfigurationSection("action.hook"), loot.getNick()));
        loot.setConsumeActions(ConfigUtils.getActions(section.getConfigurationSection("action.consume"), loot.getNick()));
        setSuccessAmountAction(section.getConfigurationSection("action.success-times"), loot);
    }

    private void setSuccessAmountAction(ConfigurationSection section, Loot loot) {
        if (section != null) {
            HashMap<Integer, Action[]> actionMap = new HashMap<>();
            for (String amount : section.getKeys(false)) {
                actionMap.put(Integer.parseInt(amount), ConfigUtils.getActions(section.getConfigurationSection(amount), loot.getNick()));
            }
            loot.setSuccessTimesActions(actionMap);
        }
    }

    private MiniGameConfig[] getMiniGames(ConfigurationSection section) {
        String[] games = section.getStringList("mini-game").size() == 0 ? new String[]{section.getString("mini-game", null)} : section.getStringList("mini-game").toArray(new String[0]);
        MiniGameConfig[] gameConfigs = new MiniGameConfig[games.length];
        for (int i = 0, size = games.length; i < size; i++) {
            if (games[i] == null) {
                return null;
            }
            MiniGameConfig gameConfig = plugin.getBarMechanicManager().getGameConfig(games[i]);
            if (gameConfig == null) {
                AdventureUtils.consoleMessage("<red>[CustomFishing] Mini game " + games[i] + " doesn't exist");
                return null;
            }
            gameConfigs[i] = gameConfig;
        }
        return gameConfigs;
    }

    public HashMap<String, Loot> getWaterLoots() {
        return waterLoots;
    }

    public HashMap<String, Loot> getLavaLoots() {
        return lavaLoots;
    }

    public ArrayList<Loot> getAllLoots() {
        ArrayList<Loot> loots = new ArrayList<>(getWaterLoots().values());
        loots.addAll(getLavaLoots().values());
        return loots;
    }

    public ArrayList<String> getAllKeys() {
        ArrayList<String> loots = new ArrayList<>();
        for (Map.Entry<String, Loot> en : CustomFishing.getInstance().getLootManager().getWaterLoots().entrySet()) {
            if (en.getValue() instanceof DroppedItem) {
                loots.add(en.getKey());
            }
        }
        for (Map.Entry<String, Loot> en : CustomFishing.getInstance().getLootManager().getLavaLoots().entrySet()) {
            if (en.getValue() instanceof DroppedItem) {
                loots.add(en.getKey());
            }
        }
        return loots;
    }

    @Nullable
    public List<String> getCategories(String categoryID) {
        return category.get(categoryID);
    }

    @NotNull
    public Loot getVanilla_loot() {
        return vanilla_loot;
    }
}
