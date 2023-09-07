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
import net.momirealms.customfishing.api.manager.EffectManager;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
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
                .effect(getEffectFromSection(section.getConfigurationSection("effects")))
                .build();
    }

    public Effect getEffectFromSection(ConfigurationSection section) {
        if (section == null) return getInitialEffect();
        return new FishingEffect.Builder()
                .lootWeightModifier(ConfigUtils.getModifiers(section.getStringList("weight")))
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
    public Effect getInitialEffect() {
        return new FishingEffect.Builder().build();
    }

    public void disable() {
        this.effectMap.clear();
    }
}
