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

package net.momirealms.customfishing.util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.Effect;
import net.momirealms.customfishing.fishing.action.*;
import net.momirealms.customfishing.fishing.requirements.*;
import net.momirealms.customfishing.helper.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigUtils {

    /**
     * Get a config by name
     * @param configName config's name
     * @return yaml
     */
    public static YamlConfiguration getConfig(String configName) {
        File file = new File(CustomFishing.getInstance().getDataFolder(), configName);
        if (!file.exists()) CustomFishing.getInstance().saveResource(configName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Update config
     * @param fileName config
     */
    public static void update(String fileName, List<String> ignoredSections) {
        try {
            YamlDocument.create(
                    new File(CustomFishing.getInstance().getDataFolder(), fileName),
                    Objects.requireNonNull(CustomFishing.getInstance().getResource(fileName)),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).addIgnoredRoute("25", "mechanics.mechanic-requirements", '.')
                            .build()
            );
        } catch (IOException e){
            Log.warn(e.getMessage());
        }
    }

    private static void setProperties(ConfigurationSection oldSec, ConfigurationSection newSec) {

    }

    /**
     * Create a data file if not exists
     * @param file file path
     * @return yaml data
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static YamlConfiguration readData(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                AdventureUtils.consoleMessage("<red>[CustomFishing] Failed to generate data files!</red>");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static RequirementInterface[] getRequirements(ConfigurationSection section) {
        if (section != null) {
            List<RequirementInterface> requirements = new ArrayList<>();
            for (String type : section.getKeys(false)) {
                switch (type) {
                    case "biome" -> requirements.add(new BiomeImpl(null, new HashSet<>(section.getStringList(type))));
                    case "weather" -> requirements.add(new WeatherImpl(null, section.getStringList(type)));
                    case "ypos" -> requirements.add(new YPosImpl(null, section.getStringList(type)));
                    case "season" -> requirements.add(new SeasonImpl(null, section.getStringList(type)));
                    case "world" -> requirements.add(new WorldImpl(null, section.getStringList(type)));
                    case "permission" -> requirements.add(new PermissionImpl(null, section.getString(type)));
                    case "time" -> requirements.add(new TimeImpl(null, section.getStringList(type)));
                    case "skill-level" -> requirements.add(new SkillLevelImpl(null, section.getInt(type)));
                    case "job-level" -> requirements.add(new JobLevelImpl(null, section.getInt(type)));
                    case "date" -> requirements.add(new DateImpl(null, new HashSet<>(section.getStringList(type))));
                    case "rod" -> requirements.add(new RodImpl(null, new HashSet<>(section.getStringList(type))));
                    case "bait" -> requirements.add(new BaitImpl(null, new HashSet<>(section.getStringList(type))));
                    case "competition" -> requirements.add(new CompetitionImpl(null, section.getBoolean(type)));
                    case "papi-condition" -> requirements.add(new CustomPapi(null, Objects.requireNonNull(section.getConfigurationSection(type)).getValues(false)));
                }
            }
            return requirements.toArray(new RequirementInterface[0]);
        }
        return null;
    }

    public static RequirementInterface[] getRequirementsWithMsg(ConfigurationSection section) {
        if (section != null) {
            List<RequirementInterface> requirements = new ArrayList<>();
            for (String id : section.getKeys(false)) {
                ConfigurationSection innerSec = section.getConfigurationSection(id);
                if (innerSec == null) continue;
                String type = innerSec.getString("type");
                if (type == null) continue;
                String[] msg = innerSec.getStringList("message").size() == 0 ? (innerSec.getString("message") == null ? null : new String[]{innerSec.getString("message")}) : innerSec.getStringList("message").toArray(new String[0]);
                switch (type) {
                    case "biome" -> requirements.add(new BiomeImpl(msg, new HashSet<>(innerSec.getStringList("value"))));
                    case "weather" -> requirements.add(new WeatherImpl(msg, innerSec.getStringList("value")));
                    case "ypos" -> requirements.add(new YPosImpl(msg, innerSec.getStringList("value")));
                    case "season" -> requirements.add(new SeasonImpl(msg, innerSec.getStringList("value")));
                    case "world" -> requirements.add(new WorldImpl(msg, innerSec.getStringList("value")));
                    case "permission" -> requirements.add(new PermissionImpl(msg, innerSec.getString("value")));
                    case "time" -> requirements.add(new TimeImpl(msg, innerSec.getStringList("value")));
                    case "skill-level" -> requirements.add(new SkillLevelImpl(msg, innerSec.getInt("value")));
                    case "job-level" -> requirements.add(new JobLevelImpl(msg, innerSec.getInt("value")));
                    case "date" -> requirements.add(new DateImpl(msg, new HashSet<>(innerSec.getStringList("value"))));
                    case "rod" -> requirements.add(new RodImpl(msg, new HashSet<>(innerSec.getStringList("value"))));
                    case "bait" -> requirements.add(new BaitImpl(msg, new HashSet<>(innerSec.getStringList("value"))));
                    case "competition" -> requirements.add(new CompetitionImpl(msg, innerSec.getBoolean("value")));
                    case "papi-condition" -> requirements.add(new CustomPapi(msg, Objects.requireNonNull(innerSec.getConfigurationSection("value")).getValues(false)));
                }
            }
            return requirements.toArray(new RequirementInterface[0]);
        }
        return null;
    }

    public static Action[] getActions(ConfigurationSection section, String nick) {
        if (section != null) {
            List<Action> actions = new ArrayList<>();
            for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection innerSec) {
                    String type = innerSec.getString("type");
                    if (type == null) continue;
                    double chance = innerSec.getDouble("chance", 1);
                    switch (type) {
                        case "message" -> {
                            actions.add(new MessageActionImpl(
                                    innerSec.getStringList("value").toArray(new String[0]),
                                    nick,
                                    chance
                            ));
                        }
                        case "command" -> {
                            actions.add(new CommandActionImpl(
                                    innerSec.getStringList("value").toArray(new String[0]),
                                    nick,
                                    chance
                            ));
                        }
                        case "exp","mending" -> {
                            actions.add(new VanillaXPImpl(
                                    innerSec.getInt("value"),
                                    type.equals("mending"),
                                    chance
                            ));
                        }
                        case "skill-xp" -> {
                            actions.add(new SkillXPImpl(
                                    innerSec.getDouble("value"),
                                    chance
                            ));
                        }
                        case "job-xp" -> {
                            actions.add(new JobXPImpl(
                                    innerSec.getDouble("value"),
                                    chance
                            ));
                        }
                        case "sound" -> {
                            actions.add(new SoundActionImpl(
                                    Sound.Source.valueOf(innerSec.getString("value.source", "PLAYER").toUpperCase(Locale.ENGLISH)),
                                    Key.key(innerSec.getString("value.key")),
                                    (float) innerSec.getDouble("value.volume"),
                                    (float) innerSec.getDouble("value.pitch"),
                                    chance
                            ));
                        }
                        case "potion-effect" -> {
                            PotionEffectType potionEffectType = PotionEffectType.getByName(innerSec.getString("value.type", "BLINDNESS").toUpperCase(Locale.ENGLISH));
                            if (potionEffectType == null) continue;
                            actions.add(
                                    new PotionEffectImpl(
                                            new PotionEffect(
                                                    potionEffectType,
                                                    innerSec.getInt("value.duration"),
                                                    innerSec.getInt("value.amplifier")
                                            ),
                                            chance
                                    )
                            );
                        }
                        case "chain" -> {
                            actions.add(new ChainImpl(
                                    getActions(innerSec.getConfigurationSection("value"), nick),
                                    chance
                            ));
                        }
                    }
                }
            }
            return actions.toArray(new Action[0]);
        }
        return null;
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
                case "score" -> effect.setScoreMultiplier(section.getDouble(modifier));
                case "size-multiply" -> effect.setSizeMultiplier(section.getDouble(modifier));
                case "lava-fishing" -> effect.setCanLavaFishing(section.getBoolean(modifier, false));
            }
        }
        return effect;
    }
}
