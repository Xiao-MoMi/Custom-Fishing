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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ConfigUtil {

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
    public static void update(String fileName){
        try {
            YamlDocument.create(new File(CustomFishing.getInstance().getDataFolder(), fileName), Objects.requireNonNull(CustomFishing.getInstance().getResource(fileName)), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e){
            Log.warn(e.getMessage());
        }
    }

    /**
     * Create a data file if not exists
     * @param file file path
     * @return yaml data
     */
    public static YamlConfiguration readData(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                AdventureUtil.consoleMessage("<red>[CustomFishing] Failed to generate data files!</red>");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static RequirementInterface[] getRequirements(ConfigurationSection section) {
        if (section != null) {
            List<RequirementInterface> requirements = new ArrayList<>();
            for (String type : section.getKeys(false)) {
                switch (type) {
                    case "biome" -> requirements.add(new BiomeImpl(null, section.getStringList(type)));
                    case "weather" -> requirements.add(new WeatherImpl(null, section.getStringList(type)));
                    case "ypos" -> requirements.add(new YPosImpl(null, section.getStringList(type)));
                    case "season" -> requirements.add(new SeasonImpl(null, section.getStringList(type)));
                    case "world" -> requirements.add(new WorldImpl(null, section.getStringList(type)));
                    case "permission" -> requirements.add(new PermissionImpl(null, section.getString(type)));
                    case "time" -> requirements.add(new TimeImpl(null, section.getStringList(type)));
                    case "skill-level" -> requirements.add(new SkillLevelImpl(null, section.getInt(type)));
                    case "job-level" -> requirements.add(new JobLevelImpl(null, section.getInt(type)));
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
                    case "biome" -> requirements.add(new BiomeImpl(msg, innerSec.getStringList("value")));
                    case "weather" -> requirements.add(new WeatherImpl(msg, innerSec.getStringList("value")));
                    case "ypos" -> requirements.add(new YPosImpl(msg, innerSec.getStringList("value")));
                    case "season" -> requirements.add(new SeasonImpl(msg, innerSec.getStringList("value")));
                    case "world" -> requirements.add(new WorldImpl(msg, innerSec.getStringList("value")));
                    case "permission" -> requirements.add(new PermissionImpl(msg, innerSec.getString("value")));
                    case "time" -> requirements.add(new TimeImpl(msg, innerSec.getStringList("value")));
                    case "skill-level" -> requirements.add(new SkillLevelImpl(msg, innerSec.getInt("value")));
                    case "job-level" -> requirements.add(new JobLevelImpl(msg, innerSec.getInt("value")));
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
            for (String action : section.getKeys(false)) {
                switch (action) {
                    case "message" -> actions.add(new MessageActionImpl(section.getStringList(action).toArray(new String[0]), nick));
                    case "command" -> actions.add(new CommandActionImpl(section.getStringList(action).toArray(new String[0]), nick));
                    case "exp" -> actions.add(new VanillaXPImpl(section.getInt(action), false));
                    case "mending" -> actions.add(new VanillaXPImpl(section.getInt(action), true));
                    case "skill-xp" -> actions.add(new SkillXPImpl(section.getDouble(action)));
                    case "job-xp" -> actions.add(new JobXPImpl(section.getDouble(action)));
                    case "sound" -> actions.add(new SoundActionImpl(
                            section.getString(action + ".source"),
                            section.getString(action + ".key"),
                            (float) section.getDouble(action + ".volume"),
                            (float) section.getDouble(action + ".pitch")
                    ));
                    case "potion-effect" -> {
                        List<PotionEffect> potionEffectList = new ArrayList<>();
                        for (String key : section.getConfigurationSection(action).getKeys(false)) {
                            PotionEffectType type = PotionEffectType.getByName(section.getString(action + "." + key + ".type", "BLINDNESS").toUpperCase());
                            if (type == null) AdventureUtil.consoleMessage("<red>[CustomFishing] Potion effect " + section.getString(action + "." + key + ".type", "BLINDNESS") + " doesn't exists");
                            potionEffectList.add(new PotionEffect(
                                    type == null ? PotionEffectType.LUCK : type,
                                    section.getInt(action + "." + key + ".duration"),
                                    section.getInt(action + "." + key + ".amplifier")
                            ));
                        }
                        actions.add(new PotionEffectImpl(potionEffectList.toArray(new PotionEffect[0])));
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
