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

package net.momirealms.customfishing.mechanic.requirement;

import net.momirealms.biomeapi.BiomeAPI;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.integration.LevelInterface;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import net.momirealms.customfishing.api.manager.RequirementManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementExpansion;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementFactory;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.papi.ParseUtils;
import net.momirealms.customfishing.util.ClassUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class RequirementManagerImpl implements RequirementManager {

    public static Requirement[] mechanicRequirements;
    private final CustomFishingPluginImpl plugin;
    private final HashMap<String, RequirementFactory> requirementBuilderMap;
    private final LinkedHashMap<String, ConditionalLoots> conditionalLootsMap;
    private final String EXPANSION_FOLDER = "expansions/requirements";

    public RequirementManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.requirementBuilderMap = new HashMap<>();
        this.conditionalLootsMap = new LinkedHashMap<>();
        this.registerInbuiltRequirements();
    }

    public void load() {
        this.loadExpansions();
        this.loadRequirementGroupFileConfig();
    }

    public void unload() {
        this.conditionalLootsMap.clear();
    }

    public void disable() {
        this.requirementBuilderMap.clear();
        this.conditionalLootsMap.clear();
    }

    public void loadRequirementGroupFileConfig() {
        YamlConfiguration main = plugin.getConfig("config.yml");
        mechanicRequirements = getRequirements(main.getConfigurationSection("mechanics.mechanic-requirements"), true);

        YamlConfiguration config = plugin.getConfig("loot-conditions.yml");
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                conditionalLootsMap.put(entry.getKey(), getConditionalLoots(section));
            }
        }
    }

    @Override
    public boolean registerRequirement(String type, RequirementFactory requirementFactory) {
        if (this.requirementBuilderMap.containsKey(type)) return false;
        this.requirementBuilderMap.put(type, requirementFactory);
        return true;
    }

    @Override
    public boolean unregisterRequirement(String type) {
        return this.requirementBuilderMap.remove(type) != null;
    }

    private void registerInbuiltRequirements() {
        this.registerTimeRequirement();
        this.registerYRequirement();
        this.registerContainRequirement();
        this.registerStartWithRequirement();
        this.registerEqualsRequirement();
        this.registerBiomeRequirement();
        this.registerDateRequirement();
        this.registerPluginLevelRequirement();
        this.registerPermissionRequirement();
        this.registerWorldRequirement();
        this.registerWeatherRequirement();
        this.registerSeasonRequirement();
        this.registerLavaFishingRequirement();
        this.registerRodRequirement();
        this.registerBaitRequirement();
        this.registerCompareRequirement();
        this.registerAndRequirement();
        this.registerOrRequirement();
        this.registerLevelRequirement();
        this.registerRandomRequirement();
        this.registerIceFishingRequirement();
        this.registerOpenWaterRequirement();
    }

    public ConditionalLoots getConditionalLoots(ConfigurationSection section) {
        var sub = section.getConfigurationSection("sub-groups");
        if (sub == null) {
            return new ConditionalLoots(
                    getRequirements(section.getConfigurationSection("conditions"), false),
                    ConfigUtils.getModifiers(section.getStringList("list")),
                    null
            );
        } else {
            HashMap<String, ConditionalLoots> subLoots = new HashMap<>();
            for (Map.Entry<String, Object> entry : sub.getValues(false).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection innerSection) {
                    subLoots.put(entry.getKey(), getConditionalLoots(innerSection));
                }
            }
            return new ConditionalLoots(
                    getRequirements(section.getConfigurationSection("conditions"), false),
                    ConfigUtils.getModifiers(section.getStringList("list")),
                    subLoots
            );
        }
    }

    @Override
    public HashMap<String, Double> getLootWithWeight(Condition condition) {
        HashMap<String, Double> lootWeightMap = new HashMap<>();
        Queue<HashMap<String, ConditionalLoots>> lootQueue = new LinkedList<>();
        lootQueue.add(conditionalLootsMap);
        Player player = condition.getPlayer();
        while (!lootQueue.isEmpty()) {
            HashMap<String, ConditionalLoots> currentLootMap = lootQueue.poll();
            for (ConditionalLoots loots : currentLootMap.values()) {
                if (loots.isConditionsMet(condition)) {
                    loots.combine(player, lootWeightMap);
                    if (loots.getSubLoots() != null) {
                        lootQueue.add(loots.getSubLoots());
                    }
                }
            }
        }
        return lootWeightMap;
    }

    @Nullable
    @Override
    public Requirement[] getRequirements(ConfigurationSection section, boolean advanced) {
        if (section == null) return null;
        List<Requirement> requirements = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            String typeOrName = entry.getKey();
            if (hasRequirement(typeOrName)) {
                requirements.add(getRequirementBuilder(typeOrName).build(entry.getValue(), null, advanced));
            } else {
                requirements.add(getRequirement(section.getConfigurationSection(typeOrName), advanced));
            }
        }
        return requirements.toArray(new Requirement[0]);
    }

    public boolean hasRequirement(String type) {
        return requirementBuilderMap.containsKey(type);
    }

    @NotNull
    @Override
    public Requirement getRequirement(ConfigurationSection section, boolean advanced) {
        if (section == null) return EmptyRequirement.instance;
        List<Action> actionList = null;
        if (advanced) {
            actionList = new ArrayList<>();
            if (section.contains("not-met-actions")) {
                for (Map.Entry<String, Object> entry : Objects.requireNonNull(section.getConfigurationSection("not-met-actions")).getValues(false).entrySet()) {
                    if (entry.getValue() instanceof MemorySection inner) {
                        actionList.add(plugin.getActionManager().getAction(inner));
                    }
                }
            }
            if (actionList.size() == 0)
                actionList = null;
        }
        String type = section.getString("type");
        if (type == null) {
            LogUtils.warn("No requirement type found at " + section.getCurrentPath());
            return EmptyRequirement.instance;
        }
        var builder = getRequirementBuilder(type);
        if (builder == null) {
            return EmptyRequirement.instance;
        }
        return builder.build(section.get("value"), actionList, advanced);
    }

    @Override
    public Requirement getRequirement(String key, Object value) {
        return getRequirementBuilder(key).build(value);
    }

    private Pair<Integer, Integer> getIntegerPair(String range) {
        String[] split = range.split("~");
        return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    @Override
    public RequirementFactory getRequirementBuilder(String type) {
        return requirementBuilderMap.get(type);
    }

    private void registerTimeRequirement() {
        registerRequirement("time", (args, actions, advanced) -> {
            List<Pair<Integer, Integer>> timePairs = ConfigUtils.stringListArgs(args).stream().map(this::getIntegerPair).toList();
            return condition -> {
                long time = condition.getLocation().getWorld().getTime();
                for (Pair<Integer, Integer> pair : timePairs)
                    if (time >= pair.left() && time <= pair.right())
                        return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerYRequirement() {
        registerRequirement("ypos", (args, actions, advanced) -> {
            List<Pair<Integer, Integer>> timePairs = ConfigUtils.stringListArgs(args).stream().map(this::getIntegerPair).toList();
            return condition -> {
                int y = condition.getLocation().getBlockY();
                for (Pair<Integer, Integer> pair : timePairs)
                    if (y >= pair.left() && y <= pair.right())
                        return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerOrRequirement() {
        registerRequirement("||", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                Requirement[] requirements = getRequirements(section, advanced);
                return condition -> {
                    if (requirements == null) return true;
                    for (Requirement requirement : requirements) {
                        if (requirement.isConditionMet(condition)) {
                            return true;
                        }
                    }
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at || requirement.");
                return null;
            }
        });
    }

    private void registerAndRequirement() {
        registerRequirement("&&", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                Requirement[] requirements = getRequirements(section, advanced);
                return condition -> {
                    if (requirements == null) return true;
                    outer: {
                        for (Requirement requirement : requirements) {
                            if (!requirement.isConditionMet(condition)) {
                                break outer;
                            }
                        }
                        return true;
                    }
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at && requirement.");
                return null;
            }
        });
    }

    private void registerLavaFishingRequirement() {
        registerRequirement("lava-fishing", (args, actions, advanced) -> {
            boolean inLava = (boolean) args;
            return condition -> {
                String current = condition.getArgs().get("{lava}");
                if (current.equals(String.valueOf(inLava)))
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerOpenWaterRequirement() {
        registerRequirement("open-water", (args, actions, advanced) -> {
            boolean inLava = (boolean) args;
            return condition -> {
                String current = condition.getArgs().get("{open-water}");
                if (current.equals(String.valueOf(inLava)))
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerIceFishingRequirement() {
        registerRequirement("ice-fishing", (args, actions, advanced) -> {
            boolean iceFishing = (boolean) args;
            return condition -> {
                Location location = condition.getLocation();
                int water = 0;
                int ice = 0;
                for (int i = -2; i <= 2; i++) {
                    for (int j = -1; j <= 2; j++) {
                        for (int k = -2; k <= 2; k++) {
                            Block block = location.clone().add(i, j, k).getBlock();
                            Material material = block.getType();
                            switch (material) {
                                case ICE -> ice++;
                                case WATER -> water++;
                            }
                        }
                    }
                }
                if ((ice >= 16 && water >= 25) == iceFishing)
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerLevelRequirement() {
        registerRequirement("level", (args, actions, advanced) -> {
            int level = (int) args;
            return condition -> {
                int current = condition.getPlayer().getLevel();
                if (current >= level)
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerRandomRequirement() {
        registerRequirement("random", (args, actions, advanced) -> {
            double random = ConfigUtils.getDoubleValue(args);
            return condition -> {
                if (Math.random() < random)
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerBiomeRequirement() {
        registerRequirement("biome", (args, actions, advanced) -> {
            HashSet<String> biomes = new HashSet<>(ConfigUtils.stringListArgs(args));
            return condition -> {
                String currentBiome = BiomeAPI.getBiome(condition.getLocation());
                    if (biomes.contains(currentBiome))
                        return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!biome", (args, actions, advanced) -> {
            HashSet<String> biomes = new HashSet<>(ConfigUtils.stringListArgs(args));
            return condition -> {
                String currentBiome = BiomeAPI.getBiome(condition.getLocation());
                if (!biomes.contains(currentBiome))
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerWorldRequirement() {
        registerRequirement("world", (args, actions, advanced) -> {
            HashSet<String> worlds = new HashSet<>(ConfigUtils.stringListArgs(args));
            return condition -> {
                if (worlds.contains(condition.getLocation().getWorld().getName()))
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!world", (args, actions, advanced) -> {
            HashSet<String> worlds = new HashSet<>(ConfigUtils.stringListArgs(args));
            return condition -> {
                if (!worlds.contains(condition.getLocation().getWorld().getName()))
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerWeatherRequirement() {
        registerRequirement("weather", (args, actions, advanced) -> {
            List<String> weathers = ConfigUtils.stringListArgs(args);
            return condition -> {
                String currentWeather;
                World world = condition.getLocation().getWorld();
                if (world.isThundering()) currentWeather = "thunder";
                else if (world.isClearWeather()) currentWeather = "clear";
                else currentWeather = "rain";
                for (String weather : weathers)
                    if (weather.equalsIgnoreCase(currentWeather))
                        return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerDateRequirement() {
        registerRequirement("date", (args, actions, advanced) -> {
            HashSet<String> dates = new HashSet<>(ConfigUtils.stringListArgs(args));
            return condition -> {
                Calendar calendar = Calendar.getInstance();
                String current = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE);
                if (dates.contains(current))
                    return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerPermissionRequirement() {
        registerRequirement("permission", (args, actions, advanced) -> {
            List<String> perms = ConfigUtils.stringListArgs(args);
            return condition -> {
                for (String perm : perms)
                    if (condition.getPlayer().hasPermission(perm))
                        return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!permission", (args, actions, advanced) -> {
            List<String> perms = ConfigUtils.stringListArgs(args);
            return condition -> {
                for (String perm : perms)
                    if (condition.getPlayer().hasPermission(perm)) {
                        if (advanced) triggerActions(actions, condition);
                        return false;
                    }
                return true;
            };
        });
    }

    private void registerSeasonRequirement() {
        registerRequirement("season", (args, actions, advanced) -> {
            List<String> seasons = ConfigUtils.stringListArgs(args);
            return condition -> {
                SeasonInterface seasonInterface = plugin.getIntegrationManager().getSeasonInterface();
                if (seasonInterface == null) return true;
                String season = seasonInterface.getSeason(condition.getLocation().getWorld());
                if (seasons.contains(season)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerCompareRequirement() {
        registerRequirement(">=", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (Double.parseDouble(p1) >= Double.parseDouble(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at >= requirement.");
                return null;
            }
        });
        registerRequirement(">", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (Double.parseDouble(p1) > Double.parseDouble(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at > requirement.");
                return null;
            }
        });
        registerRequirement("<", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (Double.parseDouble(p1) < Double.parseDouble(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at < requirement.");
                return null;
            }
        });
        registerRequirement("<=", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (Double.parseDouble(p1) <= Double.parseDouble(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at <= requirement.");
                return null;
            }
        });
        registerRequirement("==", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (Double.parseDouble(p1) == Double.parseDouble(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at !startsWith requirement.");
                return null;
            }
        });
        registerRequirement("!=", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (Double.parseDouble(p1) != Double.parseDouble(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at !startsWith requirement.");
                return null;
            }
        });
    }


    private void registerStartWithRequirement() {
        registerRequirement("startsWith", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (p1.startsWith(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at startsWith requirement.");
                return null;
            }
        });
        registerRequirement("!startsWith", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (!p1.startsWith(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at !startsWith requirement.");
                return null;
            }
        });
        registerRequirement("endsWith", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (p1.endsWith(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at endsWith requirement.");
                return null;
            }
        });
        registerRequirement("!endsWith", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (!p1.endsWith(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at !endsWith requirement.");
                return null;
            }
        });
    }

    private void registerContainRequirement() {
        registerRequirement("contains", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (p1.contains(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at contains requirement.");
                return null;
            }
        });
        registerRequirement("!contains", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (!p1.contains(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at !contains requirement.");
                return null;
            }
        });
    }

    private void registerEqualsRequirement() {
        registerRequirement("equals", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (p1.equals(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at equals requirement.");
                return null;
            }
        });
        registerRequirement("!equals", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("value1", "");
                String v2 = section.getString("value2", "");
                return condition -> {
                    String p1 = v1.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v1) : v1;
                    String p2 = v2.startsWith("%") ? ParseUtils.setPlaceholders(condition.getPlayer(), v2) : v2;
                    if (!p1.equals(p2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at !equals requirement.");
                return null;
            }
        });
    }

    private void registerRodRequirement() {
        registerRequirement("rod", (args, actions, advanced) -> {
            List<String> rods = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("{rod}");
                if (rods.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!rod", (args, actions, advanced) -> {
            List<String> rods = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("{rod}");
                if (!rods.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerBaitRequirement() {
        registerRequirement("bait", (args, actions, advanced) -> {
            List<String> baits = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("{bait}");
                if (baits.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!bait", (args, actions, advanced) -> {
            List<String> baits = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("{bait}");
                if (!baits.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerPluginLevelRequirement() {
        registerRequirement("plugin-level", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String pluginName = section.getString("plugin");
                int level = section.getInt("level");
                String target = section.getString("target");
                return condition -> {
                    LevelInterface levelInterface = plugin.getIntegrationManager().getLevelHook(pluginName);
                    if (levelInterface == null) {
                        LogUtils.warn("Plugin (" + pluginName + "'s) level is not compatible. Please double check if it's a problem caused by pronunciation.");
                        return true;
                    }
                    if (levelInterface.getLevel(condition.getPlayer(), target) >= level)
                        return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at plugin-level requirement.");
                return null;
            }
        });
    }

    private void triggerActions(List<Action> actions, Condition condition) {
        if (actions != null)
            for (Action action : actions)
                action.trigger(condition);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadExpansions() {
        File expansionFolder = new File(plugin.getDataFolder(), EXPANSION_FOLDER);
        if (!expansionFolder.exists())
            expansionFolder.mkdirs();

        List<Class<? extends RequirementExpansion>> classes = new ArrayList<>();
        File[] expansionJars = expansionFolder.listFiles();
        if (expansionJars == null) return;
        for (File expansionJar : expansionJars) {
            if (expansionJar.getName().endsWith(".jar")) {
                try {
                    Class<? extends RequirementExpansion> expansionClass = ClassUtils.findClass(expansionJar, RequirementExpansion.class);
                    classes.add(expansionClass);
                } catch (IOException | ClassNotFoundException e) {
                    LogUtils.warn("Failed to load expansion: " + expansionJar.getName(), e);
                }
            }
        }
        try {
            for (Class<? extends RequirementExpansion> expansionClass : classes) {
                RequirementExpansion expansion = expansionClass.getDeclaredConstructor().newInstance();
                unregisterRequirement(expansion.getRequirementType());
                registerRequirement(expansion.getRequirementType(), expansion.getRequirementFactory());
                LogUtils.info("Loaded requirement expansion: " + expansion.getRequirementType() + "[" + expansion.getVersion() + "]" + " by " + expansion.getAuthor());
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            LogUtils.warn("Error occurred when creating expansion instance.", e);
        }
    }
}
