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
import net.momirealms.customfishing.integration.BlockInterface;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.action.ActionInterface;
import net.momirealms.customfishing.object.action.CommandActionImpl;
import net.momirealms.customfishing.object.action.MessageActionImpl;
import net.momirealms.customfishing.object.requirements.*;
import net.momirealms.customfishing.object.totem.CorePos;
import net.momirealms.customfishing.object.totem.FinalModel;
import net.momirealms.customfishing.object.totem.OriginalModel;
import net.momirealms.customfishing.object.totem.Totem;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ConfigUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TotemManager extends Function {

    public static HashMap<String, Totem> TOTEMS;
    public static HashMap<String, List<Totem>> CORES;
    public static HashMap<String, String> BLOCKS;
    public static HashMap<String, String> INVERTED;

    @Override
    public void unload() {
        if (TOTEMS != null) TOTEMS.clear();
        if (CORES != null) CORES.clear();
        if (BLOCKS != null) BLOCKS.clear();
        if (INVERTED != null) INVERTED.clear();
    }

    @Override
    public void load(){
        TOTEMS = new HashMap<>();
        CORES = new HashMap<>();
        BLOCKS = new HashMap<>();
        INVERTED = new HashMap<>();
        loadBlocks();
        loadTotems();
    }

    private void loadBlocks() {
        YamlConfiguration config = ConfigUtil.getConfig("totem-blocks.yml");
        config.getKeys(false).forEach(key -> BLOCKS.put(key, config.getString(key)));
        config.getKeys(false).forEach(key -> INVERTED.put(config.getString(key), key));
    }

    private void loadTotems() {
        YamlConfiguration config = ConfigUtil.getConfig("totems.yml");
        for (String key : config.getKeys(false)) {
            List<String> cores = config.getStringList(key + ".core");
            List<String> flat = config.getStringList(key + ".layer.1");
            int length = flat.get(0).split("\\s+").length;
            int width = flat.size();
            int height = Objects.requireNonNull(config.getConfigurationSection(key + ".layer")).getKeys(false).size();
            CorePos corePos = null;
            OriginalModel originalModel = new OriginalModel(length, width, height);
            FinalModel finalModel = new FinalModel(length, width, height);
            for (int k = 0; k < height; k++) {
                List<String> layer = config.getStringList(key + ".layer." + (k+1));
                if (layer.size() != width) {
                    AdventureUtil.consoleMessage("<red>[CustomFishing] Each layer should have the same size! Error exists in totem:" + key + " layer:" + (k + 1));
                    return;
                }
                for (int j = 0; j < width; j++) {
                    String[] args = layer.get(j).split("\\s+");
                    if (args.length != length) {
                        AdventureUtil.consoleMessage("<red>[CustomFishing] Each layer should have the same size! Error exists in totem:" + key + " layer:" + (k + 1) + " line:" + (k + 1));
                        return;
                    }
                    for (int i = 0; i < length; i++) {
                        if (args[i].startsWith("(") && args[i].endsWith(")")){
                            String content = args[i].substring(1, args[i].length()-1);
                            corePos = getCorePos(cores, corePos, originalModel, k, j, i, content);
                            finalModel.setElement("*", i, j, k);
                        }
                        else if (args[i].contains(">")) {
                            String before = StringUtils.split(args[i],">")[0];
                            String after = StringUtils.split(args[i],">")[1];
                            finalModel.setElement(after, i, j, k);
                            corePos = getCorePos(cores, corePos, originalModel, k, j, i, before);
                        }
                        else {
                            String[] elements = StringUtils.split(args[i], "|");
                            originalModel.setElement(elements, i, j, k);
                            for (String core : cores) {
                                for (String element : elements) {
                                    if (element.equals(core)) {
                                        corePos = new CorePos(i, j, k);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(corePos == null) {
                AdventureUtil.consoleMessage("<red>[CustomTotems] No core block set for totem:" + key);
                return;
            }
            else {
                finalModel.setCorePos(corePos);
                originalModel.setCorePos(corePos);
            }

            Totem totem = new Totem(
                    originalModel,
                    finalModel,
                    config.getInt(key + ".radius", 16),
                    config.getInt(key + ".duration", 300),
                    Particle.valueOf(config.getString(key + ".particle", "SPELL_MOB").toUpperCase()),
                    BonusManager.getBonus(config, key)
            );

            List<ActionInterface> actionList = new ArrayList<>();
            List<ActionInterface> nearActionList = new ArrayList<>();
            if (config.contains(key + ".action")) {
                for (String action : config.getConfigurationSection(key + ".action").getKeys(false)) {
                    switch (action) {
                        case "commands-activator" -> actionList.add(new CommandActionImpl(config.getStringList(key + ".action." + action).toArray(new String[0]), null));
                        case "commands-nearby-players" -> nearActionList.add(new CommandActionImpl(config.getStringList(key + ".action." + action).toArray(new String[0]), null));
                        case "messages-activator" -> actionList.add(new MessageActionImpl(config.getStringList(key + ".action." + action).toArray(new String[0]), null));
                        case "messages-nearby-players" -> nearActionList.add(new MessageActionImpl(config.getStringList(key + ".action." + action).toArray(new String[0]), null));
                    }
                }
            }
            totem.setActivatorActions(actionList.toArray(new ActionInterface[0]));
            totem.setNearbyActions(nearActionList.toArray(new ActionInterface[0]));

            if (config.contains(key + ".requirements")) {
                List<RequirementInterface> requirements = new ArrayList<>();
                config.getConfigurationSection(key + ".requirements").getKeys(false).forEach(requirement -> {
                    switch (requirement){
                        case "weather" -> requirements.add(new WeatherImpl(config.getStringList(key + ".requirements.weather")));
                        case "ypos" -> requirements.add(new YPosImpl(config.getStringList(key + ".requirements.ypos")));
                        case "season" -> requirements.add(new SeasonImpl(config.getStringList(key + ".requirements.season")));
                        case "world" -> requirements.add(new WorldImpl(config.getStringList(key + ".requirements.world")));
                        case "biome" -> requirements.add(new BiomeImpl(config.getStringList(key + ".requirements.biome")));
                        case "permission" -> requirements.add(new PermissionImpl(config.getString(key + ".requirements.permission")));
                        case "time" -> requirements.add(new TimeImpl(config.getStringList(key + ".requirements.time")));
                        case "skill-level" -> requirements.add(new SkillLevelImpl(config.getInt(key + ".requirements.skill-level")));
                        case "papi-condition" -> requirements.add(new CustomPapi(config.getConfigurationSection(key + ".requirements.papi-condition").getValues(false)));
                    }
                });
                totem.setRequirements(requirements.toArray(new RequirementInterface[0]));
            }

            if (config.getBoolean(key + ".hologram.enable", false)) {
                totem.setHoloText(config.getStringList(key + ".hologram.text").toArray(new String[0]));
                totem.setHoloOffset(config.getDouble(key + ".hologram.y-offset"));
            }

            if (config.contains(key + ".potion-effects")) {
                List<PotionEffect> potionEffectList = new ArrayList<>();
                for (String potion : config.getConfigurationSection(key + ".potion-effects").getKeys(false)) {

                    PotionEffectType potionType = PotionEffectType.getByName(potion.toUpperCase());
                    if (potionType == null) continue;
                    int time = 40;
                    if (potionType.equals(PotionEffectType.NIGHT_VISION)) time = 400;
                    PotionEffect potionEffect = new PotionEffect(
                            potionType,
                            time,
                            config.getInt(key + ".potion-effects." + potion, 1) - 1);
                    potionEffectList.add(potionEffect);
                }
                totem.setPotionEffects(potionEffectList.toArray(new PotionEffect[0]));
            }

            TOTEMS.put(key, totem);

            for (String core : cores) {
                if (CORES.get(core) == null){
                    List<Totem> totems = new ArrayList<>();
                    totems.add(totem);
                    CORES.put(core, totems);
                }
                else {
                    CORES.get(core).add(totem);
                }
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + TOTEMS.size() + " <gray>totems");
    }

    private CorePos getCorePos(List<String> cores, CorePos corePos, OriginalModel originalModel, int k, int j, int i, String content) {
        String[] elements = StringUtils.split(content, "|");
        originalModel.setElement(elements, i, j, k);
        for (String core : cores) {
            for (String element : elements) {
                if (element.equals(core)) {
                    corePos = new CorePos(i, j, k);
                }
            }
        }
        return corePos;
    }

    public int checkLocationModel(OriginalModel model, Location location){

        BlockInterface blockInterface = CustomFishing.plugin.getIntegrationManager().getBlockInterface();

        CorePos corePos = model.getCorePos();
        int xOffset = corePos.getX();
        int yOffset = corePos.getY();
        int zOffset = corePos.getZ();

        int height = model.getHeight();
        int length = model.getLength();
        int width = model.getWidth();

        Location startLoc = location.clone().subtract(0, yOffset, 0);

        Label_1:
        {
            for(int i = 0; i< height; i++) {
                Location loc = startLoc.clone().add(-xOffset, i, -zOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(x, 0, z).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_1;
                    }
                }
            }
            return 1;
        }

        Label_2:
        {
            for (int i = 0; i < height; i++) {
                Location loc = startLoc.clone().add(xOffset, i, zOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(-x, 0, -z).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_2;
                    }
                }
            }
            return 2;
        }

        Label_3:
        {
            for (int i = 0; i < height; i++) {
                Location loc = startLoc.clone().add(-zOffset, i, xOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(z, 0, -x).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_3;
                    }
                }
            }
            return 3;
        }

        Label_4:
        {
            for (int i = 0; i < height; i++) {
                Location loc = startLoc.clone().add(zOffset, i, -xOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(-z, 0, x).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_4;
                    }
                }
            }
            return 4;
        }

        Label_5:
        {
            for (int i = 0; i < height; i++) {
                Location loc = startLoc.clone().add(-zOffset, i, -xOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(z, 0, x).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_5;
                    }
                }
            }
            return 5;
        }

        Label_6:
        {
            for (int i = 0; i < height; i++) {
                Location loc = startLoc.clone().add(zOffset, i, xOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(-z, 0, -x).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_6;
                    }
                }
            }
            return 6;
        }

        Label_7:
        {
            for (int i = 0; i < height; i++) {
                Location loc = startLoc.clone().add(-xOffset, i, zOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(x, 0, -z).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_7;
                    }
                }
            }
            return 7;
        }
        Label_8:
        {
            for (int i = 0; i < height; i++) {
                Location loc = startLoc.clone().add(xOffset, i, -zOffset);
                for (int z = 0; z < width; z++) {
                    inner: for (int x = 0; x < length; x++) {
                        String[] elements = model.getElement(x, z, i);
                        String id = blockInterface.getID(loc.clone().add(-x, 0, z).getBlock());
                        for (String element : elements) {
                            if (element.equals("*")) continue inner;
                            if (id == null) break;
                            if (id.equals(element)) continue inner;
                        }
                        break Label_8;
                    }
                }
            }
            return 8;
        }
        return 0;
    }


    public void removeModel(FinalModel model, Location location, int id) {

        BlockInterface blockInterface = CustomFishing.plugin.getIntegrationManager().getBlockInterface();

        CorePos corePos = model.getCorePos();
        int xOffset = corePos.getX();
        int yOffset = corePos.getY();
        int zOffset = corePos.getZ();

        int height = model.getHeight();
        int length = model.getLength();
        int width = model.getWidth();

        Location startLoc = location.clone().subtract(0, yOffset, 0);

        switch (id) {
            case 1:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(-xOffset, i, -zOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(x, 0, z).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(x, 0, z), model.getElement(x, z, i));
                            }
                        }
                }
                break;
            case 2:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(xOffset, i, zOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(-x, 0, -z).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(-x, 0, -z), model.getElement(x, z, i));
                            }
                        }
                }
                break;
            case 3:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(-zOffset, i, xOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(z, 0, -x).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(z, 0, -x), model.getElement(x, z, i));
                            }
                        }
                }
                break;
            case 4:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(zOffset, i, -xOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(-z, 0, x).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(-z, 0, x), model.getElement(x, z, i));
                            }
                        }
                }
                break;
            case 5:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(-zOffset, i, -xOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(z, 0, x).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(z, 0, x), model.getElement(x, z, i));
                            }
                        }
                }
                break;
            case 6:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(zOffset, i, xOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(-z, 0, -x).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(-z, 0, -x), model.getElement(x, z, i));
                            }
                        }
                }
                break;
            case 7:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(-xOffset, i, zOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(x, 0, -z).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(x, 0, -z), model.getElement(x, z, i));
                            }
                        }
                }
                break;
            case 8:
                for (int i = 0; i < height; i++) {
                    Location loc = startLoc.clone().add(xOffset, i, -zOffset);
                    for (int z = 0; z < width; z++)
                        for (int x = 0; x < length; x++) {
                            if (model.getElement(x, z, i) == null) {
                                blockInterface.removeBlock(loc.clone().add(-x, 0, z).getBlock());
                            }
                            else if (!model.getElement(x, z, i).equals("*")){
                                blockInterface.replaceBlock(loc.clone().add(-x, 0, z), model.getElement(x, z, i));
                            }
                        }
                }
                break;
        }
    }
}
