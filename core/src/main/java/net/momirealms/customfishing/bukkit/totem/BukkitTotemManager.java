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

package net.momirealms.customfishing.bukkit.totem;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.totem.TotemConfig;
import net.momirealms.customfishing.api.mechanic.totem.TotemManager;
import net.momirealms.customfishing.api.mechanic.totem.TotemModel;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import net.momirealms.customfishing.api.mechanic.totem.block.property.AxisImpl;
import net.momirealms.customfishing.api.mechanic.totem.block.property.FaceImpl;
import net.momirealms.customfishing.api.mechanic.totem.block.property.HalfImpl;
import net.momirealms.customfishing.api.mechanic.totem.block.property.TotemBlockProperty;
import net.momirealms.customfishing.api.mechanic.totem.block.type.TypeCondition;
import net.momirealms.customfishing.api.util.SimpleLocation;
import net.momirealms.customfishing.bukkit.totem.particle.DustParticleSetting;
import net.momirealms.customfishing.bukkit.totem.particle.ParticleSetting;
import net.momirealms.customfishing.bukkit.util.LocationUtils;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BukkitTotemManager implements TotemManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, List<TotemConfig>> totemConfigMap;
    private final List<String> allMaterials;
    private final ConcurrentHashMap<SimpleLocation, ActivatedTotem> activatedTotems;
    private SchedulerTask timerCheckTask;

    public BukkitTotemManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.totemConfigMap = new HashMap<>();
        this.allMaterials = Arrays.stream(Material.values()).map(Enum::name).toList();
        this.activatedTotems = new ConcurrentHashMap<>();
    }

    @Override
    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
        this.timerCheckTask = plugin.getScheduler().asyncRepeating(() -> {
            long time = System.currentTimeMillis();
            ArrayList<SimpleLocation> removed = new ArrayList<>();
            for (Map.Entry<SimpleLocation, ActivatedTotem> entry : activatedTotems.entrySet()) {
                if (time > entry.getValue().getExpireTime()) {
                    removed.add(entry.getKey());
                    entry.getValue().cancel();
                } else {
                    entry.getValue().doTimerAction();
                }
            }
            for (SimpleLocation simpleLocation : removed) {
                activatedTotems.remove(simpleLocation);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
        for (ActivatedTotem activatedTotem : this.activatedTotems.values())
            activatedTotem.cancel();
        this.activatedTotems.clear();
        if (this.timerCheckTask != null)
            this.timerCheckTask.cancel();
        this.totemConfigMap.clear();
    }

    @Override
    public Collection<String> getActivatedTotems(Location location) {
        Collection<String> activated = new ArrayList<>();
        double nearest = Double.MAX_VALUE;
        String nearestTotemID = null;
        for (ActivatedTotem activatedTotem : activatedTotems.values()) {
            double distance = LocationUtils.getDistance(activatedTotem.getCoreLocation(), location);
            if (distance < activatedTotem.getRadius()) {
               activated.add(activatedTotem.getTotemConfig().id());
               if (nearest > distance) {
                   nearest = distance;
                   nearestTotemID = activatedTotem.getTotemConfig().id();
               }
            }
        }
        if (nearestTotemID == null) return List.of();
        if (!ConfigManager.allowMultipleTotemType()) {
            if (ConfigManager.allowSameTotemType()) {
                String finalNearestTotemID = nearestTotemID;
                activated.removeIf(element -> !element.equals(finalNearestTotemID));
                return activated;
            } else {
                return List.of(nearestTotemID);
            }
        } else {
            if (ConfigManager.allowSameTotemType()) {
                return activated;
            } else {
                return new HashSet<>(activated);
            }
        }
    }

    @EventHandler
    public void onBreakTotemCore(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Location location = event.getBlock().getLocation();
        SimpleLocation simpleLocation = SimpleLocation.of(location);
        ActivatedTotem activatedTotem = activatedTotems.remove(simpleLocation);
        if (activatedTotem != null)
            activatedTotem.cancel();
    }

    @EventHandler (ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (
            event.isBlockInHand() ||
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK ||
            event.getHand() != EquipmentSlot.HAND
        )
            return;

        Block block = event.getClickedBlock();
        assert block != null;
        String id = plugin.getBlockManager().getBlockID(block);
        List<TotemConfig> configs = totemConfigMap.get(id);
        if (configs == null)
            return;
        TotemConfig config = null;
        for (TotemConfig temp : configs) {
            if (temp.isRightPattern(block.getLocation())) {
                config = temp;
                break;
            }
        }
        if (config == null)
            return;
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("totem")) {
            File typeFolder = new File(plugin.getBoostrap().getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.getBoostrap().saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
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
        YamlDocument config = plugin.getConfigManager().loadData(file);
        for (Map.Entry<String, Object> entry : config.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section section) {
                TotemConfig totemConfig = TotemConfig.builder()
                        .id(entry.getKey())
                        .totemModels(getTotemModels(section.getSection("pattern")))
                        .activateRequirements(plugin.getRequirementManager().parseRequirements(section.getSection("requirements"), true))
                        .radius(MathValue.auto(section.get("radius", 8.0)))
                        .duration(MathValue.auto(section.get("duration", 100)))
                        .particleSettings(getParticleSettings(section.getSection("particles")))
                        .build();

                HashSet<String> coreMaterials = new HashSet<>();
                for (TotemBlock totemBlock : totemConfig.totemCore()) {
                    String text = totemBlock.getTypeCondition().getRawText();
                    if (text.startsWith("*")) {
                        String sub = text.substring(1);
                        coreMaterials.addAll(allMaterials.stream().filter(it -> it.endsWith(sub)).toList());
                    } else if (text.endsWith("*")) {
                        String sub = text.substring(0, text.length() - 1);
                        coreMaterials.addAll(allMaterials.stream().filter(it -> it.startsWith(sub)).toList());
                    } else {
                        coreMaterials.add(text);
                    }
                }
                for (String material : coreMaterials) {
                    putTotemConfigToMap(material, totemConfig);
                }
            }
        }
    }

    private void putTotemConfigToMap(String material, TotemConfig totemConfig) {
        List<TotemConfig> configs = this.totemConfigMap.getOrDefault(material, new ArrayList<>());
        configs.add(totemConfig);
        this.totemConfigMap.put(material, configs);
    }

    public ParticleSetting[] getParticleSettings(Section section) {
        List<ParticleSetting> particleSettings = new ArrayList<>();
        if (section != null)
            for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof Section innerSection) {
                    particleSettings.add(getParticleSetting(innerSection));
                }
            }
        return particleSettings.toArray(new ParticleSetting[0]);
    }

    public ParticleSetting getParticleSetting(Section section) {
        Particle particle = Particle.valueOf(section.getString("type","REDSTONE"));
        String formulaHorizontal = section.getString("polar-coordinates-formula.horizontal");
        String formulaVertical = section.getString("polar-coordinates-formula.vertical");
        List<Pair<Double, Double>> ranges = section.getStringList("theta.range")
                .stream().map(it -> {
                    String[] split = it.split("~");
                    return Pair.of(Double.parseDouble(split[0]) * Math.PI / 180, Double.parseDouble(split[1]) * Math.PI / 180);
                }).toList();

        double interval = section.getDouble("theta.draw-interval", 3d);
        int delay = section.getInt("task.delay", 0);
        int period = section.getInt("task.period", 0);
        if (particle == Particle.REDSTONE) {
            String color = section.getString("options.color","0,0,0");
            String[] colorSplit = color.split(",");
            return new DustParticleSetting(
                    formulaHorizontal,
                    formulaVertical,
                    particle,
                    interval,
                    ranges,
                    delay,
                    period,
                    new Particle.DustOptions(
                            Color.fromRGB(
                                    Integer.parseInt(colorSplit[0]),
                                    Integer.parseInt(colorSplit[1]),
                                    Integer.parseInt(colorSplit[2])
                            ),
                            section.getDouble("options.scale", 1.0).floatValue()
                    )
            );
        } else if (particle == Particle.DUST_COLOR_TRANSITION) {
            String color = section.getString("options.from","0,0,0");
            String[] colorSplit = color.split(",");
            String toColor = section.getString("options.to","255,255,255");
            String[] toColorSplit = toColor.split(",");
            return new DustParticleSetting(
                    formulaHorizontal,
                    formulaVertical,
                    particle,
                    interval,
                    ranges,
                    delay,
                    period,
                    new Particle.DustTransition(
                            Color.fromRGB(
                                    Integer.parseInt(colorSplit[0]),
                                    Integer.parseInt(colorSplit[1]),
                                    Integer.parseInt(colorSplit[2])
                            ),
                            Color.fromRGB(
                                    Integer.parseInt(toColorSplit[0]),
                                    Integer.parseInt(toColorSplit[1]),
                                    Integer.parseInt(toColorSplit[2])
                            ),
                            section.getDouble("options.scale", 1.0).floatValue()
                    )
            );
        } else {
            return new ParticleSetting(
                    formulaHorizontal,
                    formulaVertical,
                    particle,
                    interval,
                    ranges,
                    delay,
                    period
            );
        }
    }

    private TotemModel[] getTotemModels(Section section) {
        TotemModel originalModel = parseModel(section);
        List<TotemModel> modelList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            originalModel = originalModel.deepClone().rotate90();
            modelList.add(originalModel);
            if (i % 2 == 0) {
                modelList.add(originalModel.mirrorVertically());
            } else {
                modelList.add(originalModel.mirrorHorizontally());
            }
        }
        return modelList.toArray(new TotemModel[0]);
    }

    @SuppressWarnings("unchecked")
    private TotemModel parseModel(Section section) {
        Section layerSection = section.getSection("layer");
        List<TotemBlock[][][]> totemBlocksList = new ArrayList<>();
        if (layerSection != null) {
            var set = layerSection.getStringRouteMappedValues(false).entrySet();
            TotemBlock[][][][] totemBlocks = new TotemBlock[set.size()][][][];
            for (Map.Entry<String, Object> entry : set) {
                if (entry.getValue() instanceof List<?> list) {
                    totemBlocks[Integer.parseInt(entry.getKey())-1] = parseLayer((List<String>) list);
                }
            }
            totemBlocksList.addAll(List.of(totemBlocks));
        }

        String[] core = section.getString("core","1,1,1").split(",");
        int x = Integer.parseInt(core[2]) - 1;
        int z = Integer.parseInt(core[1]) - 1;
        int y = Integer.parseInt(core[0]) - 1;
        return new TotemModel(
                x,y,z,
                totemBlocksList.toArray(new TotemBlock[0][][][])
        );
    }

    private TotemBlock[][][] parseLayer(List<String> lines) {
        List<TotemBlock[][]> totemBlocksList = new ArrayList<>();
        for (String line : lines) {
            totemBlocksList.add(parseSingleLine(line));
        }
        return totemBlocksList.toArray(new TotemBlock[0][][]);
    }

    private TotemBlock[][] parseSingleLine(String line) {
        List<TotemBlock[]> totemBlocksList = new ArrayList<>();
        String[] splits = line.split("\\s+");
        for (String split : splits) {
            totemBlocksList.add(parseSingleElement(split));
        }
        return totemBlocksList.toArray(new TotemBlock[0][]);
    }

    private TotemBlock[] parseSingleElement(String element) {
        String[] orBlocks = element.split("\\|\\|");
        List<TotemBlock> totemBlockList = new ArrayList<>();
        for (String block : orBlocks) {
            int index = block.indexOf("{");
            List<TotemBlockProperty> propertyList = new ArrayList<>();
            if (index == -1) {
                index = block.length();
            } else {
                String propertyStr = block.substring(index+1, block.length()-1);
                String[] properties = propertyStr.split(";");
                for (String property : properties) {
                    String[] split = property.split("=");
                    if (split.length < 2) continue;
                    String key = split[0];
                    String value = split[1];
                    switch (key) {
                        // Block face
                        case "face" -> {
                            BlockFace blockFace = BlockFace.valueOf(value.toUpperCase(Locale.ENGLISH));
                            propertyList.add(new FaceImpl(blockFace));
                        }
                        // Block axis
                        case "axis" -> {
                            Axis axis = Axis.valueOf(value.toUpperCase(Locale.ENGLISH));
                            propertyList.add(new AxisImpl(axis));
                        }
                        // Slab, Stair half
                        case "half" -> {
                            Bisected.Half half = Bisected.Half.valueOf(value.toUpperCase(Locale.ENGLISH));
                            propertyList.add(new HalfImpl(half));
                        }
                    }
                }
            }
            String type = block.substring(0, index);
            TotemBlock totemBlock = new TotemBlock(
                    TypeCondition.getTypeCondition(type),
                    propertyList.toArray(new TotemBlockProperty[0])
            );
            totemBlockList.add(totemBlock);
        }
        return totemBlockList.toArray(new TotemBlock[0]);
    }
}
