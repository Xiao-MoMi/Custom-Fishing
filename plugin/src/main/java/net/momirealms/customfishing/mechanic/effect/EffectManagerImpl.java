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

package net.momirealms.customfishing.mechanic.effect;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.EffectManager;
import net.momirealms.customfishing.api.mechanic.GlobalSettings;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
import net.momirealms.customfishing.api.mechanic.loot.WeightModifier;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class EffectManagerImpl implements EffectManager {

    private final CustomFishingPlugin plugin;

    private final HashMap<Key, EffectCarrier> effectMap;

    public EffectManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.effectMap = new HashMap<>();
    }

    public void disable() {
        this.effectMap.clear();
    }

    /**
     * Registers an EffectCarrier with a unique Key.
     *
     * @param key    The unique Key associated with the EffectCarrier.
     * @param effect The EffectCarrier to be registered.
     * @return True if the registration was successful, false if the Key already exists.
     */
    @Override
    public boolean registerEffectCarrier(Key key, EffectCarrier effect) {
        if (effectMap.containsKey(key)) return false;
        this.effectMap.put(key, effect);
        return true;
    }

    /**
     * Unregisters an EffectCarrier associated with the specified Key.
     *
     * @param key The unique Key of the EffectCarrier to unregister.
     * @return True if the EffectCarrier was successfully unregistered, false if the Key does not exist.
     */
    @Override
    public boolean unregisterEffectCarrier(Key key) {
        return this.effectMap.remove(key) != null;
    }

    /**
     * Checks if an EffectCarrier with the specified namespace and id exists.
     *
     * @param namespace The namespace of the EffectCarrier.
     * @param id        The unique identifier of the EffectCarrier.
     * @return True if an EffectCarrier with the given namespace and id exists, false otherwise.
     */
    @Override
    public boolean hasEffectCarrier(String namespace, String id) {
        return effectMap.containsKey(Key.of(namespace, id));
    }

    /**
     * Retrieves an EffectCarrier with the specified namespace and id.
     *
     * @param namespace The namespace of the EffectCarrier.
     * @param id        The unique identifier of the EffectCarrier.
     * @return The EffectCarrier with the given namespace and id, or null if it doesn't exist.
     */
    @Nullable
    @Override
    public EffectCarrier getEffectCarrier(String namespace, String id) {
        return effectMap.get(Key.of(namespace, id));
    }

    public void load() {
        this.loadFiles();
        this.loadGlobalEffects();
    }

    /**
     * Loads EffectCarrier configurations from YAML files in different content folders.
     * EffectCarrier configurations are organized by type (rod, bait, enchant, util, totem, hook) in separate folders.
     * Each YAML file within these folders is processed to populate the effectMap.
     */
    @SuppressWarnings("DuplicatedCode")
    private void loadFiles() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("rod", "bait", "enchant", "util", "totem", "hook")) {
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
                        this.loadSingleFile(subFile, type);
                    }
                }
            }
        }
    }

    /**
     * Loads EffectCarrier configurations from a YAML file and populates the effectMap.
     *
     * @param file      The YAML file to load configurations from.
     * @param namespace The namespace to use when creating keys for EffectCarriers.
     */
    private void loadSingleFile(File file, String namespace) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : yaml.getValues(false).entrySet()) {
            String value = entry.getKey();
            if (entry.getValue() instanceof ConfigurationSection section) {
                Key key = Key.of(namespace, value);
                EffectCarrier item = getEffectCarrierFromSection(key, section);
                if (item != null)
                    effectMap.put(key, item);
            }
        }
    }

    /**
     * Parses a ConfigurationSection to create an EffectCarrier based on the specified key and configuration.
     *
     * @param key     The key that uniquely identifies the EffectCarrier.
     * @param section The ConfigurationSection containing the EffectCarrier configuration.
     * @return An EffectCarrier instance based on the key and configuration, or null if the section is null.
     */
    @Override
    @Nullable
    public EffectCarrier getEffectCarrierFromSection(Key key, ConfigurationSection section) {
        if (section == null) return null;
        return new EffectCarrier.Builder()
                .key(key)
                .requirements(plugin.getRequirementManager().getRequirements(section.getConfigurationSection("requirements"), true))
                .effect(getEffectModifiers(section.getConfigurationSection("effects")))
                .actionMap(plugin.getActionManager().getActionMap(section.getConfigurationSection("events")))
                .build();
    }

    public void unload() {
        HashMap<Key, EffectCarrier> temp = new HashMap<>(effectMap);
        effectMap.clear();
        for (Map.Entry<Key, EffectCarrier> entry : temp.entrySet()) {
            if (entry.getValue().isPersist()) {
                effectMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Retrieves the initial FishingEffect that represents no special effects.
     *
     * @return The initial FishingEffect.
     */
    @NotNull
    @Override
    public FishingEffect getInitialEffect() {
        return new FishingEffect();
    }

    /**
     * Retrieves a list of modifiers based on specified loot groups.
     *
     * @param modList A list of strings containing group modifiers in the format "group:modifier".
     * @return A list of pairs where each pair represents a loot item and its associated modifier.
     */
    private List<Pair<String, WeightModifier>> getGroupModifiers(List<String> modList) {
        List<Pair<String, WeightModifier>> result = new ArrayList<>();
        for (String group : modList) {
            String[] split = group.split(":",2);
            String key = split[0];
            List<String> members = plugin.getLootManager().getLootGroup(key);
            if (members == null) {
                LogUtils.warn("Group " + key + " doesn't contain any loot. The effect would not take effect.");
                return result;
            }
            for (String loot : members) {
                result.add(Pair.of(loot, ConfigUtils.getModifier(split[1])));
            }
        }
        return result;
    }

    /**
     * Parses a ConfigurationSection to retrieve an array of EffectModifiers.
     *
     * @param section The ConfigurationSection to parse.
     * @return An array of EffectModifiers based on the values found in the section.
     */
    @NotNull
    @Override
    public EffectModifier[] getEffectModifiers(ConfigurationSection section) {
        if (section == null) return new EffectModifier[0];
        ArrayList<EffectModifier> modifiers = new ArrayList<>();
        for (Map.Entry<String, Object> entry: section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection inner) {
                EffectModifier effectModifier = getEffectModifier(inner);
                if (effectModifier != null)
                    modifiers.add(effectModifier);
            }
        }
        return modifiers.toArray(new EffectModifier[0]);
    }

    private void loadGlobalEffects() {
        YamlConfiguration config = plugin.getConfig("config.yml");
        ConfigurationSection section = config.getConfigurationSection("mechanics.global-effects");
        GlobalSettings.setEffects(getEffectModifiers(section));
    }

    /**
     * Parses a ConfigurationSection to create an EffectModifier based on the specified type and configuration.
     *
     * @param section The ConfigurationSection containing the effect modifier configuration.
     * @return An EffectModifier instance based on the type and configuration.
     */
    @Override
    @Nullable
    public EffectModifier getEffectModifier(ConfigurationSection section) {
        String type = section.getString("type");
        if (type == null) return null;
        switch (type) {
            case "weight-mod" -> {
                var modList = ConfigUtils.getModifiers(section.getStringList("value"));
                return ((effect, condition) -> {
                    effect.addWeightModifier(modList);
                });
            }
            case "weight-mod-ignore-conditions" -> {
                var modList = ConfigUtils.getModifiers(section.getStringList("value"));
                return ((effect, condition) -> {
                    effect.addWeightModifierIgnored(modList);
                });
            }
            case "group-mod" -> {
                var modList = getGroupModifiers(section.getStringList("value"));
                return ((effect, condition) -> {
                    effect.addWeightModifier(modList);
                });
            }
            case "group-mod-ignore-conditions" -> {
                var modList = getGroupModifiers(section.getStringList("value"));
                return ((effect, condition) -> {
                    effect.addWeightModifierIgnored(modList);
                });
            }
            case "wait-time" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setWaitTime(effect.getWaitTime() + value.get(condition.getPlayer()));
                });
            }
            case "hook-time", "wait-time-multiplier" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setWaitTimeMultiplier(effect.getWaitTimeMultiplier() + value.get(condition.getPlayer()) - 1);
                });
            }
            case "difficulty" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setDifficulty(effect.getDifficulty() + value.get(condition.getPlayer()));
                });
            }
            case "difficulty-multiplier", "difficulty-bonus" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setDifficultyMultiplier(effect.getDifficultyMultiplier() + value.get(condition.getPlayer()) - 1);
                });
            }
            case "multiple-loot" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setMultipleLootChance(effect.getMultipleLootChance() + value.get(condition.getPlayer()));
                });
            }
            case "score" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setScore(effect.getScore() + value.get(condition.getPlayer()));
                });
            }
            case "score-bonus", "score-multiplier" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setScoreMultiplier(effect.getScoreMultiplier() + value.get(condition.getPlayer()) - 1);
                });
            }
            case "size" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setSize(effect.getSize() + value.get(condition.getPlayer()));
                });
            }
            case "size-bonus", "size-multiplier" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setSizeMultiplier(effect.getSizeMultiplier() + value.get(condition.getPlayer()) - 1);
                });
            }
            case "game-time" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setGameTime(effect.getGameTime() + value.get(condition.getPlayer()));
                });
            }
            case "game-time-bonus", "game-time-multiplier" -> {
                var value = ConfigUtils.getValue(section.get("value"));
                return ((effect, condition) -> {
                    effect.setGameTimeMultiplier(effect.getGameTimeMultiplier() + value.get(condition.getPlayer()) - 1);
                });
            }
            case "lava-fishing" -> {
                return ((effect, condition) -> effect.setLavaFishing(true));
            }
            case "conditional" -> {
                Requirement[] requirements = plugin.getRequirementManager().getRequirements(section.getConfigurationSection("conditions"), true);
                EffectModifier[] modifiers = getEffectModifiers(section.getConfigurationSection("effects"));
                return ((effect, condition) -> {
                    for (Requirement requirement : requirements)
                        if (!requirement.isConditionMet(condition))
                            return;
                    for (EffectModifier modifier : modifiers) {
                        modifier.modify(effect, condition);
                    }
                });
            }
            default -> {
                LogUtils.warn("Effect " + type + " doesn't exist.");
                return null;
            }
        }
    }
}
