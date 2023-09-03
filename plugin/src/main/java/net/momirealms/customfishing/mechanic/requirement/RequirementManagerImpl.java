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
 *
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
import net.momirealms.customfishing.api.mechanic.requirement.RequirementBuilder;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.mechanic.requirement.inbuilt.LogicRequirement;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequirementManagerImpl implements RequirementManager {

    public static Requirement[] mechanicRequirements;
    private final CustomFishingPluginImpl plugin;
    private final HashMap<String, RequirementBuilder> requirementBuilderMap;
    private final LinkedHashMap<String, ConditionalLoots> conditionalLootsMap;

    public RequirementManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.requirementBuilderMap = new HashMap<>();
        this.conditionalLootsMap = new LinkedHashMap<>();
        this.registerInbuiltRequirements();
    }

    public void load() {
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
    public boolean registerRequirement(String type, RequirementBuilder requirementBuilder) {
        if (this.requirementBuilderMap.containsKey(type)) return false;
        this.requirementBuilderMap.put(type, requirementBuilder);
        return true;
    }

    @Override
    public boolean unregisterRequirement(String type) {
        return this.requirementBuilderMap.remove(type) != null;
    }

    private void registerInbuiltRequirements() {
        this.registerTimeRequirement();
        this.registerYRequirement();
        this.registerLogicRequirement();
        this.registerCompare();
        this.registerBiomeRequirement();
        this.registerDateRequirement();
        this.registerPluginLevelRequirement();
        this.registerPermissionRequirement();
        this.registerWorldRequirement();
        this.registerWeatherRequirement();
        this.registerSeasonRequirement();
        this.registerInLavaRequirement();
        this.registerRodRequirement();
        this.registerBaitRequirement();
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
        while (!lootQueue.isEmpty()) {
            HashMap<String, ConditionalLoots> currentLootMap = lootQueue.poll();
            for (ConditionalLoots loots : currentLootMap.values()) {
                if (loots.isConditionsMet(condition)) {
                    loots.combine(lootWeightMap);
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
        List<Action> actionList = null;
        if (advanced) {
            actionList = new ArrayList<>();
            if (section.contains("actions")) {
                for (Map.Entry<String, Object> entry : Objects.requireNonNull(section.getConfigurationSection("actions")).getValues(false).entrySet()) {
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
            throw new NullPointerException(section.getCurrentPath() + ".type" + " doesn't exist");
        }
        var builder = getRequirementBuilder(type);
        if (builder == null) {
            throw new NullPointerException("Requirement type: " + type + " doesn't exist");
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
    public RequirementBuilder getRequirementBuilder(String type) {
        return requirementBuilderMap.get(type);
    }

    private void registerLogicRequirement() {
        registerRequirement("logic", (args, actions, advanced) ->
                new LogicRequirement(this, args, actions, advanced)
        );
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

    private void registerInLavaRequirement() {
        registerRequirement("in-lava", (args, actions, advanced) -> {
            boolean inLava = (boolean) args;
            return condition -> {
                String current = condition.getArgs().get("in-lava");
                if (current.equals(String.valueOf(inLava)))
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

    private void registerRodRequirement() {
        registerRequirement("rod", (args, actions, advanced) -> {
            List<String> rods = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("rod");
                if (rods.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!rod", (args, actions, advanced) -> {
            List<String> rods = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("rod");
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
                String id = condition.getArg("bait");
                if (baits.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!bait", (args, actions, advanced) -> {
            List<String> baits = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("bait");
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
            }
            return null;
        });
    }

    private void registerCompare() {
        registerRequirement("compare", (args, actions, advanced) -> condition -> {
            if (evaluateExpression((String) args, condition.getPlayer()))
                return true;
            if (advanced) triggerActions(actions, condition);
            return false;
        });
    }

    private void triggerActions(List<Action> actions, Condition condition) {
        if (actions != null)
            for (Action action : actions)
                action.trigger(condition);
    }

    private double doubleArg(String s, Player player) {
        double arg = 0;
        try {
            arg = Double.parseDouble(s);
        } catch (NumberFormatException e1) {
            try {
                arg = Double.parseDouble(plugin.getPlaceholderManager().setPlaceholders(player, s));
            } catch (NumberFormatException e2) {
                LogUtils.severe(String.format("Invalid placeholder %s", s), e2);
            }
        }
        return arg;
    }

    public boolean evaluateExpression(String input, Player player) {
        input = input.replace("\\s", "");
        Pattern pattern = Pattern.compile("(-?\\d+\\.?\\d*)(==|!=|<=?|>=?)(-?\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            double num1 = doubleArg(matcher.group(1), player);
            String operator = matcher.group(2);
            double num2 = doubleArg(matcher.group(3), player);
            return switch (operator) {
                case ">" -> num1 > num2;
                case "<" -> num1 < num2;
                case ">=" -> num1 >= num2;
                case "<=" -> num1 <= num2;
                case "==" -> num1 == num2;
                case "!=" -> num1 != num2;
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            };
        } else {
            throw new IllegalArgumentException("Invalid input format: " + input);
        }
    }
}
