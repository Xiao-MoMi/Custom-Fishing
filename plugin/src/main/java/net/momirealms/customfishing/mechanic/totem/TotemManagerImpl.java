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

package net.momirealms.customfishing.mechanic.totem;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.TotemManager;
import net.momirealms.customfishing.mechanic.totem.block.AxisImpl;
import net.momirealms.customfishing.mechanic.totem.block.FaceImpl;
import net.momirealms.customfishing.mechanic.totem.block.TotemBlock;
import net.momirealms.customfishing.mechanic.totem.block.TotemBlockProperty;
import net.momirealms.customfishing.mechanic.totem.block.type.TypeCondition;
import net.momirealms.customfishing.util.MatrixUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class TotemManagerImpl implements TotemManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, TotemConfig> totemConfigMap;

    public TotemManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.totemConfigMap = new HashMap<>();
    }

    public void load() {
        this.loadConfig();
    }

    public void unload() {
        this.totemConfigMap.clear();
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("totem")) {
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
                        this.loadSingleFile(subFile);
                    }
                }
            }
        }
    }

    private void loadSingleFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    }

    public TotemBlock[][][][][] getMirroredRotatedModels(ConfigurationSection section) {
        TotemBlock[][][][] originalModel = parseModel(section);
        List<TotemBlock[][][][]> modelList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            TotemBlock[][][][] tempModel = SerializationUtils.clone(originalModel);
            for (TotemBlock[][][] totemBlocks : tempModel) {
                TotemBlock[][][] rotatedTotemBlocks = MatrixUtils.rotate90(totemBlocks);
                for (TotemBlock[][] totemBlocks1 : rotatedTotemBlocks) {
                    for (TotemBlock[] totemBlocks2 : totemBlocks1) {
                        for (TotemBlock totemBlock : totemBlocks2) {
                            totemBlock.rotate90();
                        }
                    }
                }
            }
            modelList.add(tempModel);

            TotemBlock[][][][] tempModel2 = SerializationUtils.clone(tempModel);
            for (TotemBlock[][][] totemBlocks : tempModel2) {
                MatrixUtils.mirrorHorizontally(totemBlocks);
            }
            modelList.add(tempModel2);
        }

        return modelList.toArray(new TotemBlock[0][][][][]);
    }

    @SuppressWarnings("unchecked")
    public TotemBlock[][][][] parseModel(ConfigurationSection section) {
        List<TotemBlock[][][]> totemBlocksList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof List<?> list) {
                totemBlocksList.add(parseLayer((List<String>) list));
            }
        }
        return totemBlocksList.toArray(new TotemBlock[0][][][]);
    }

    public TotemBlock[][][] parseLayer(List<String> lines) {
        List<TotemBlock[][]> totemBlocksList = new ArrayList<>();
        for (String line : lines) {
            totemBlocksList.add(parseSingleLine(line));
        }
        return totemBlocksList.toArray(new TotemBlock[0][][]);
    }

    public TotemBlock[][] parseSingleLine(String line) {
        List<TotemBlock[]> totemBlocksList = new ArrayList<>();
        String[] splits = line.split("\\s");
        for (String split : splits) {
            totemBlocksList.add(parseSingleElement(split));
        }
        return totemBlocksList.toArray(new TotemBlock[0][]);
    }

    public TotemBlock[] parseSingleElement(String element) {
        String[] orBlocks = element.split("\\\\");
        List<TotemBlock> totemBlockList = new ArrayList<>();
        for (String block : orBlocks) {
            int index = block.indexOf("{");
            String type = block.substring(0, index-1);
            String propertyStr = block.substring(index+1, block.length()-1);
            String[] properties = propertyStr.split(";");
            List<TotemBlockProperty> propertyList = new ArrayList<>();
            for (String property : properties) {
                String[] split = property.split("=");
                if (split.length <= 2) continue;
                String key = split[0];
                String value = split[1];
                switch (key) {
                    case "face" -> {
                        BlockFace blockFace = BlockFace.valueOf(value.toUpperCase(Locale.ENGLISH));
                        propertyList.add(new FaceImpl(blockFace));
                    }
                    case "axis" -> {
                        Axis axis = Axis.valueOf(value.toUpperCase(Locale.ENGLISH));
                        propertyList.add(new AxisImpl(axis));
                    }
                }
            }
            TotemBlock totemBlock = new TotemBlock(
                    TypeCondition.getTypeCondition(type),
                    propertyList.toArray(new TotemBlockProperty[0])
            );
            totemBlockList.add(totemBlock);
        }
        return totemBlockList.toArray(new TotemBlock[0]);
    }

    public void disable() {
    }
}
