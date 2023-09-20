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
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.common.SimpleLocation;
import net.momirealms.customfishing.api.manager.TotemManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.mechanic.totem.block.TotemBlock;
import net.momirealms.customfishing.mechanic.totem.block.property.AxisImpl;
import net.momirealms.customfishing.mechanic.totem.block.property.FaceImpl;
import net.momirealms.customfishing.mechanic.totem.block.property.HalfImpl;
import net.momirealms.customfishing.mechanic.totem.block.property.TotemBlockProperty;
import net.momirealms.customfishing.mechanic.totem.block.type.TypeCondition;
import net.momirealms.customfishing.mechanic.totem.particle.DustParticleSetting;
import net.momirealms.customfishing.mechanic.totem.particle.ParticleSetting;
import net.momirealms.customfishing.util.LocationUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TotemManagerImpl implements TotemManager, Listener {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, List<TotemConfig>> totemConfigMap;
    private final List<String> allMaterials;
    private final ConcurrentHashMap<SimpleLocation, ActivatedTotem> activatedTotems;
    private CancellableTask timerCheckTask;

    public TotemManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.totemConfigMap = new HashMap<>();
        this.allMaterials = Arrays.stream(Material.values()).map(Enum::name).toList();
        this.activatedTotems = new ConcurrentHashMap<>();
    }

    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.timerCheckTask = plugin.getScheduler().runTaskAsyncTimer(() -> {
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

    public void unload() {
        this.totemConfigMap.clear();
        for (ActivatedTotem activatedTotem : activatedTotems.values()) {
            activatedTotem.cancel();
        }
        activatedTotems.clear();
        HandlerList.unregisterAll(this);
        if (this.timerCheckTask != null && !this.timerCheckTask.isCancelled())
            this.timerCheckTask.cancel();
    }

    public void disable() {
        unload();
    }

    /**
     * Get the EffectCarrier associated with an activated totem located near the specified location.
     *
     * @param location The location to search for activated totems.
     * @return The EffectCarrier associated with the nearest activated totem or null if none are found.
     */
    @Override
    @Nullable
    public EffectCarrier getTotemEffect(Location location) {
        for (ActivatedTotem activatedTotem : activatedTotems.values()) {
            if (LocationUtils.getDistance(activatedTotem.getCoreLocation(), location) < activatedTotem.getTotemConfig().getRadius()) {
                return activatedTotem.getEffectCarrier();
            }
        }
        return null;
    }

    @EventHandler
    public void onBreakTotemCore(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Location location = event.getBlock().getLocation();
        SimpleLocation simpleLocation = SimpleLocation.getByBukkitLocation(location);
        ActivatedTotem activatedTotem = activatedTotems.remove(simpleLocation);
        if (activatedTotem != null)
            activatedTotem.cancel();
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.isBlockInHand())
            return;
        if (event.useItemInHand() == Event.Result.DENY)
            return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        Block block = event.getClickedBlock();
        assert block != null;
        String id = plugin.getBlockManager().getAnyPluginBlockID(block);
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
        String totemKey = config.getKey();
        EffectCarrier carrier = plugin.getEffectManager().getEffectCarrier("totem", totemKey);
        if (carrier == null)
            return;
        Condition condition = new Condition(block.getLocation(), event.getPlayer(), new HashMap<>());
        if (!carrier.isConditionMet(condition))
            return;
        Action[] actions = carrier.getActionMap().get(ActionTrigger.ACTIVATE);
        if (actions != null)
            for (Action action : actions) {
                action.trigger(condition);
            }
        Location location = block.getLocation();
        ActivatedTotem activatedTotem = new ActivatedTotem(location, config);
        SimpleLocation simpleLocation = SimpleLocation.getByBukkitLocation(location);

        ActivatedTotem previous = this.activatedTotems.put(simpleLocation, activatedTotem);
        if (previous != null) {
            previous.cancel();
        }
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
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                TotemConfig totemConfig = new TotemConfig.Builder(entry.getKey())
                        .setTotemModels(getTotemModels(section.getConfigurationSection("pattern")))
                        .setParticleSettings(getParticleSettings(section.getConfigurationSection("particles")))
                        .setRequirements(plugin.getRequirementManager().getRequirements(section.getConfigurationSection("requirements"), true))
                        .setRadius(section.getDouble("radius", 8))
                        .setDuration(section.getInt("duration", 300))
                        .build();

                HashSet<String> coreMaterials = new HashSet<>();
                for (TotemBlock totemBlock : totemConfig.getTotemCore()) {
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

    public ParticleSetting[] getParticleSettings(ConfigurationSection section) {
        List<ParticleSetting> particleSettings = new ArrayList<>();
        if (section != null)
            for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection innerSection) {
                    particleSettings.add(getParticleSetting(innerSection));
                }
            }
        return particleSettings.toArray(new ParticleSetting[0]);
    }

    public ParticleSetting getParticleSetting(ConfigurationSection section) {
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
                            (float) section.getDouble("options.scale", 1)
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
                            (float) section.getDouble("options.scale", 1)
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

    public TotemModel[] getTotemModels(ConfigurationSection section) {
        TotemModel originalModel = parseModel(section);
        List<TotemModel> modelList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            originalModel = originalModel.clone().rotate90();
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
    public TotemModel parseModel(ConfigurationSection section) {
        ConfigurationSection layerSection = section.getConfigurationSection("layer");
        List<TotemBlock[][][]> totemBlocksList = new ArrayList<>();
        if (layerSection != null) {
            var set = layerSection.getValues(false).entrySet();
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

    public TotemBlock[][][] parseLayer(List<String> lines) {
        List<TotemBlock[][]> totemBlocksList = new ArrayList<>();
        for (String line : lines) {
            totemBlocksList.add(parseSingleLine(line));
        }
        return totemBlocksList.toArray(new TotemBlock[0][][]);
    }

    public TotemBlock[][] parseSingleLine(String line) {
        List<TotemBlock[]> totemBlocksList = new ArrayList<>();
        String[] splits = line.split("\\s+");
        for (String split : splits) {
            totemBlocksList.add(parseSingleElement(split));
        }
        return totemBlocksList.toArray(new TotemBlock[0][]);
    }

    public TotemBlock[] parseSingleElement(String element) {
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
