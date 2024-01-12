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

package net.momirealms.customfishing.mechanic.loot;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.LootManager;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.CFLoot;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import net.momirealms.customfishing.api.mechanic.loot.WeightModifier;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsKey;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.WeightUtils;
import net.momirealms.customfishing.mechanic.requirement.RequirementManagerImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class LootManagerImpl implements LootManager {

    private final CustomFishingPlugin plugin;
    // A map that associates loot IDs with their respective loot configurations.
    private final HashMap<String, Loot> lootMap;
    // A map that associates loot group IDs with lists of loot IDs.
    private final HashMap<String, List<String>> lootGroupMap;

    public LootManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.lootMap = new HashMap<>();
        this.lootGroupMap = new HashMap<>();
    }

    public void load() {
        this.loadLootsFromPluginFolder();
    }

    public void unload() {
        this.lootMap.clear();
        this.lootGroupMap.clear();
    }

    public void disable() {
        unload();
    }

    /**
     * Loads loot configurations from the plugin's content folders.
     * This method scans the "item," "entity," and "block" subfolders within the plugin's data folder
     * and loads loot configurations from YAML files.
     * If the subfolders or default loot files don't exist, it creates them.
     */
    @SuppressWarnings("DuplicatedCode")
    public void loadLootsFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("item", "entity", "block")) {
            File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
            }
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                        loadSingleFile(subFile, type);
                    }
                }
            }
        }
    }

    /**
     * Retrieves a list of loot IDs associated with a loot group key.
     *
     * @param key The key of the loot group.
     * @return A list of loot IDs belonging to the specified loot group, or null if not found.
     */
    @Nullable
    @Override
    public List<String> getLootGroup(String key) {
        return lootGroupMap.get(key);
    }

    /**
     * Retrieves a loot configuration based on a provided loot key.
     *
     * @param key The key of the loot configuration.
     * @return The Loot object associated with the specified loot key, or null if not found.
     */
    @Nullable
    @Override
    public Loot getLoot(String key) {
        return lootMap.get(key);
    }

    /**
     * Retrieves a collection of all loot configuration keys.
     *
     * @return A collection of all loot configuration keys.
     */
    @Override
    public Collection<String> getAllLootKeys() {
        return lootMap.keySet();
    }

    /**
     * Retrieves a collection of all loot configurations.
     *
     * @return A collection of all loot configurations.
     */
    @Override
    public Collection<Loot> getAllLoots() {
        return lootMap.values();
    }

    /**
     * Retrieves loot configurations with weights based on a given condition.
     *
     * @param condition The condition used to filter loot configurations.
     * @return A mapping of loot configuration keys to their associated weights.
     */
    @Override
    public HashMap<String, Double> getLootWithWeight(Condition condition) {
        return ((RequirementManagerImpl) plugin.getRequirementManager()).getLootWithWeight(condition);
    }

    /**
     * Get a collection of possible loot keys based on a given condition.
     *
     * @param condition The condition to determine possible loot.
     * @return A collection of loot keys.
     */
    @Override
    public Collection<String> getPossibleLootKeys(Condition condition) {
        return ((RequirementManagerImpl) plugin.getRequirementManager()).getLootWithWeight(condition).keySet();
    }

    /**
     * Get a map of possible loot keys with their corresponding weights, considering fishing effect and condition.
     *
     * @param initialEffect The effect to apply weight modifiers.
     * @param condition     The condition to determine possible loot.
     * @return A map of loot keys and their weights.
     */
    @NotNull
    @Override
    public Map<String, Double> getPossibleLootKeysWithWeight(Effect initialEffect, Condition condition) {
        Map<String, Double> lootWithWeight = ((RequirementManagerImpl) plugin.getRequirementManager()).getLootWithWeight(condition);
        Player player = condition.getPlayer();
        for (Pair<String, WeightModifier> pair : initialEffect.getWeightModifier()) {
            Double previous = lootWithWeight.get(pair.left());
            if (previous != null)
                lootWithWeight.put(pair.left(), pair.right().modify(player, previous));
        }
        for (Pair<String, WeightModifier> pair : initialEffect.getWeightModifierIgnored()) {
            double previous = lootWithWeight.getOrDefault(pair.left(), 0d);
            lootWithWeight.put(pair.left(), pair.right().modify(player, previous));
        }
        return lootWithWeight;
    }

    /**
     * Get the next loot item based on fishing effect and condition.
     *
     * @param initialEffect The effect to apply weight modifiers.
     * @param condition     The condition to determine possible loot.
     * @return The next loot item, or null if it doesn't exist.
     */
    @Override
    @Nullable
    public Loot getNextLoot(Effect initialEffect, Condition condition) {
        String key = WeightUtils.getRandom(getPossibleLootKeysWithWeight(initialEffect, condition));
        if (key == null) {
            LogUtils.warn("No loot available at " + condition.getLocation() + " for player: " + condition.getPlayer().getName());
            return null;
        }
        Loot loot = getLoot(key);
        if (loot == null) {
            LogUtils.warn(String.format("Loot %s doesn't exist in any of the subfolders[item/entity/block].", key));
            return null;
        }
        return loot;
    }

    /**
     * Loads loot configurations from a single YAML file and populates the lootMap and lootGroupMap.
     *
     * @param file      The YAML file containing loot configurations.
     * @param namespace The namespace indicating the type of loot (e.g., "item," "entity," "block").
     */
    private void loadSingleFile(File file, String namespace) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : yaml.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                var loot = getSingleSectionItem(
                        file.getPath(),
                        section,
                        namespace,
                        entry.getKey()
                );
                // Check for duplicate loot configurations and log an error if found.
                if (lootMap.containsKey(entry.getKey())) {
                    LogUtils.severe("Duplicated loot found: " + entry.getKey() + ".");
                } else {
                    lootMap.put(entry.getKey(), loot);
                }
                String[] group = loot.getLootGroup();
                // If the loot configuration belongs to one or more groups, update lootGroupMap.
                if (group != null) {
                    for (String g : group) {
                        List<String> groupMembers = lootGroupMap.computeIfAbsent(g, k -> new ArrayList<>());
                        groupMembers.add(loot.getID());
                    }
                }
                // legacy format support
                if (section.contains("requirements") && section.contains("weight")) {
                    plugin.getRequirementManager().putLegacyLootToMap(
                            loot.getID(),
                            plugin.getRequirementManager().getRequirements(section.getConfigurationSection("requirements"), false),
                            section.getDouble("weight", 0)
                    );
                }
            }
        }
    }

    /**
     * Creates a single loot configuration item from a ConfigurationSection.
     *
     * @param section   The ConfigurationSection containing loot configuration data.
     * @param namespace The namespace indicating the type of loot (e.g., "item," "entity," "block").
     * @param key       The unique key identifying the loot configuration.
     * @return A CFLoot object representing the loot configuration.
     */
    private CFLoot getSingleSectionItem(String filePath, ConfigurationSection section, String namespace, String key) {
        return new CFLoot.Builder(key, LootType.valueOf(namespace.toUpperCase(Locale.ENGLISH)))
                .filePath(filePath)
                .disableStats(section.getBoolean("disable-stat", CFConfig.globalDisableStats))
                .disableGames(section.getBoolean("disable-game", CFConfig.globalDisableGame))
                .instantGame(section.getBoolean("instant-game", CFConfig.globalInstantGame))
                .showInFinder(section.getBoolean("show-in-fishfinder", CFConfig.globalShowInFinder))
                .disableGlobalActions(section.getBoolean("disable-global-event", false))
                .score(section.getDouble("score"))
                .lootGroup(ConfigUtils.stringListArgs(section.get("group")).toArray(new String[0]))
                .nick(section.getString("nick", section.getString("display.name", key)))
                .addActions(plugin.getActionManager().getActionMap(section.getConfigurationSection("events")))
                .addTimesActions(plugin.getActionManager().getTimesActionMap(section.getConfigurationSection("events.success-times")))
                .statsKey(new StatisticsKey(section.getString("statistics.amount", key), section.getString("statistics.size", key)))
                .build();
    }
}
