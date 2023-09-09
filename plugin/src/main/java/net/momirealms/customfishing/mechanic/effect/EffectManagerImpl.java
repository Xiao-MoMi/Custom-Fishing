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
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
import net.momirealms.customfishing.api.mechanic.loot.WeightModifier;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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

    @Override
    public boolean registerEffectItem(Key key, EffectCarrier effect) {
        if (effectMap.containsKey(key)) return false;
        this.effectMap.put(key, effect);
        return true;
    }

    @Override
    public boolean unregisterEffectItem(Key key) {
        return this.effectMap.remove(key) != null;
    }

    @Nullable
    @Override
    public EffectCarrier getEffect(String namespace, String id) {
        return effectMap.get(Key.of(namespace, id));
    }

    @SuppressWarnings("DuplicatedCode")
    public void load() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("rods", "baits", "enchants", "utils", "totems")) {
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
                        this.loadSingleFile(subFile, StringUtils.chop(type));
                    }
                }
            }
        }
    }

    private void loadSingleFile(File file, String namespace) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : yaml.getValues(false).entrySet()) {
            String value = entry.getKey();
            if (entry.getValue() instanceof ConfigurationSection section) {
                Key key = Key.of(namespace, value);
                EffectCarrier item = getEffectItemFromSection(key, section);
                if (item != null)
                    effectMap.put(key, item);
            }
        }
    }

    private EffectCarrier getEffectItemFromSection(Key key, ConfigurationSection section) {
        if (section == null) return null;
        return new EffectCarrier.Builder()
                .key(key)
                .requirements(plugin.getRequirementManager().getRequirements(section.getConfigurationSection("requirements"), true))
                .effect(getEffectModifiers(section.getConfigurationSection("effects")))
                .actionMap(plugin.getActionManager().getActionMap(section.getConfigurationSection("events")))
                .build();
    }

    public Effect getEffectFromSection(ConfigurationSection section) {
        if (section == null) return getInitialEffect();
        return new FishingEffect.Builder()
                .addWeightModifier(ConfigUtils.getModifiers(section.getStringList("weight-single")))
                .addWeightModifier(getGroupModifiers(section.getStringList("weight-group")))
                .addWeightModifierIgnored(ConfigUtils.getModifiers(section.getStringList("weight-single-ignore-condition")))
                .addWeightModifierIgnored(getGroupModifiers(section.getStringList("weight-group-ignore-condition")))
                .timeModifier(section.getDouble("hook-time", 1))
                .difficultyModifier(section.getDouble("difficulty", 0))
                .multipleLootChance(section.getDouble("multiple-loot", 0))
                .lavaFishing(section.getBoolean("lava-fishing", false))
                .scoreMultiplier(section.getDouble("score-bonus", 1))
                .sizeMultiplier(section.getDouble("size-bonus", 1))
                .gameTimeModifier(section.getDouble("game-time", 0))
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

    @Override
    public FishingEffect getInitialEffect() {
        return new FishingEffect.Builder().build();
    }

    private List<Pair<String, WeightModifier>> getGroupModifiers(List<String> modList) {
        List<Pair<String, WeightModifier>> result = new ArrayList<>();
        for (String group : modList) {
            String[] split = group.split(":",2);
            String key = split[0];
            List<String> members = plugin.getLootManager().getLootGroup(key);
            if (members == null) {
                LogUtils.warn("Group " + key + " doesn't include any loot. The effect would not take effect.");
                return result;
            }
            for (String loot : members) {
                result.add(Pair.of(loot, ConfigUtils.getModifier(split[1])));
            }
        }
        return result;
    }

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
            case "hook-time" -> {
                double time = section.getDouble("value");
                return ((effect, condition) -> {
                    effect.setHookTimeModifier(effect.getHookTimeModifier() + time - 1);
                });
            }
            case "difficulty" -> {
                double difficulty = section.getDouble("value");
                return ((effect, condition) -> {
                    effect.setDifficultyModifier(effect.getDifficultyModifier() + difficulty);
                });
            }
            case "multiple-loot" -> {
                double multiple = section.getDouble("value");
                return ((effect, condition) -> {
                    effect.setMultipleLootChance(effect.getMultipleLootChance() + multiple);
                });
            }
            case "score-bonus" -> {
                double multiple = section.getDouble("value");
                return ((effect, condition) -> {
                    effect.setScoreMultiplier(effect.getScoreMultiplier() + multiple - 1);
                });
            }
            case "size-bonus" -> {
                double multiple = section.getDouble("value");
                return ((effect, condition) -> {
                    effect.setSizeMultiplier(effect.getSizeMultiplier() + multiple - 1);
                });
            }
            case "game-time" -> {
                double time = section.getDouble("value");
                return ((effect, condition) -> {
                    effect.setGameTimeModifier(effect.getGameTimeModifier() + time);
                });
            }
            case "lava-fishing" -> {
                return ((effect, condition) -> effect.setLavaFishing(true));
            }
            case "conditional" -> {
                Requirement[] requirements = plugin.getRequirementManager().getRequirements(section.getConfigurationSection("conditions"), false);
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
