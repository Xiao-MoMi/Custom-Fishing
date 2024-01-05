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
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementExpansion;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementFactory;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.VaultHook;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private final LinkedHashMap<String, ConditionalElement> conditionalLootsMap;
    private final LinkedHashMap<String, ConditionalElement> conditionalGamesMap;
    private final String EXPANSION_FOLDER = "expansions/requirement";

    public RequirementManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.requirementBuilderMap = new HashMap<>();
        this.conditionalLootsMap = new LinkedHashMap<>();
        this.conditionalGamesMap = new LinkedHashMap<>();
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

    @Override
    public boolean putLegacyLootToMap(String key, Requirement[] requirements, double weight) {
        if (conditionalLootsMap.containsKey("LEGACY_" + key)) {
            return false;
        } else {
            conditionalLootsMap.put("LEGACY_" + key, new ConditionalElement(requirements, List.of(Pair.of(key, (player, origin) -> weight + origin)), new HashMap<>()));
            return true;
        }
    }

    /**
     * Loads requirement group configuration data from various configuration files.
     */
    public void loadRequirementGroupFileConfig() {
        // Load mechanic requirements from the main configuration file
        YamlConfiguration main = plugin.getConfig("config.yml");
        mechanicRequirements = getRequirements(main.getConfigurationSection("mechanics.mechanic-requirements"), true);

        // Load conditional loot data from the loot conditions configuration file
        YamlConfiguration config1 = plugin.getConfig("loot-conditions.yml");
        for (Map.Entry<String, Object> entry : config1.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                conditionalLootsMap.put(entry.getKey(), getConditionalElements(section));
            }
        }

        // Load conditional game data from the game conditions configuration file
        YamlConfiguration config2 = plugin.getConfig("game-conditions.yml");
        for (Map.Entry<String, Object> entry : config2.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                conditionalGamesMap.put(entry.getKey(), getConditionalElements(section));
            }
        }
    }

    /**
     * Registers a custom requirement type with its corresponding factory.
     *
     * @param type               The type identifier of the requirement.
     * @param requirementFactory The factory responsible for creating instances of the requirement.
     * @return True if registration was successful, false if the type is already registered.
     */
    @Override
    public boolean registerRequirement(String type, RequirementFactory requirementFactory) {
        if (this.requirementBuilderMap.containsKey(type)) return false;
        this.requirementBuilderMap.put(type, requirementFactory);
        return true;
    }

    /**
     * Unregisters a custom requirement type.
     *
     * @param type The type identifier of the requirement to unregister.
     * @return True if unregistration was successful, false if the type is not registered.
     */
    @Override
    public boolean unregisterRequirement(String type) {
        return this.requirementBuilderMap.remove(type) != null;
    }

    /**
     * Retrieves a ConditionalElement from a given ConfigurationSection.
     *
     * @param section The ConfigurationSection containing the conditional element data.
     * @return A ConditionalElement instance representing the data in the section.
     */
    private ConditionalElement getConditionalElements(ConfigurationSection section) {
        var sub = section.getConfigurationSection("sub-groups");
        if (sub == null) {
            return new ConditionalElement(
                    getRequirements(section.getConfigurationSection("conditions"), false),
                    ConfigUtils.getModifiers(section.getStringList("list")),
                    null
            );
        } else {
            HashMap<String, ConditionalElement> subElements = new HashMap<>();
            for (Map.Entry<String, Object> entry : sub.getValues(false).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection innerSection) {
                    subElements.put(entry.getKey(), getConditionalElements(innerSection));
                }
            }
            return new ConditionalElement(
                    getRequirements(section.getConfigurationSection("conditions"), false),
                    ConfigUtils.getModifiers(section.getStringList("list")),
                    subElements
            );
        }
    }

    private void registerInbuiltRequirements() {
        this.registerTimeRequirement();
        this.registerYRequirement();
        this.registerContainRequirement();
        this.registerStartWithRequirement();
        this.registerEndWithRequirement();
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
        this.registerGreaterThanRequirement();
        this.registerAndRequirement();
        this.registerOrRequirement();
        this.registerLevelRequirement();
        this.registerRandomRequirement();
        this.registerIceFishingRequirement();
        this.registerOpenWaterRequirement();
        this.registerCoolDownRequirement();
        this.registerGroupRequirement();
        this.registerLootRequirement();
        this.registerLessThanRequirement();
        this.registerNumberEqualRequirement();
        this.registerRegexRequirement();
        this.registerItemInHandRequirement();
        this.registerMoneyRequirement();
        this.registerInBagRequirement();
        this.registerHookRequirement();
        this.registerCompetitionRequirement();
        this.registerListRequirement();
        this.registerEnvironmentRequirement();
        this.registerPotionEffectRequirement();
        this.registerSizeRequirement();
        this.registerHasStatsRequirement();
        this.registerLootTypeRequirement();
    }

    public HashMap<String, Double> getLootWithWeight(Condition condition) {
        return getString2DoubleMap(condition, conditionalLootsMap);
    }

    public HashMap<String, Double> getGameWithWeight(Condition condition) {
        return getString2DoubleMap(condition, conditionalGamesMap);
    }

    /**
     * Retrieves a mapping of strings to doubles based on conditional elements and a player's condition.
     *
     * @param condition The player's condition.
     * @param conditionalGamesMap The map of conditional elements representing loots/games.
     * @return A HashMap with strings as keys and doubles as values representing loot/game weights.
     */
    @NotNull
    private HashMap<String, Double> getString2DoubleMap(Condition condition, LinkedHashMap<String, ConditionalElement> conditionalGamesMap) {
        HashMap<String, Double> lootWeightMap = new HashMap<>();
        Queue<HashMap<String, ConditionalElement>> lootQueue = new LinkedList<>();
        lootQueue.add(conditionalGamesMap);
        Player player = condition.getPlayer();
        while (!lootQueue.isEmpty()) {
            HashMap<String, ConditionalElement> currentLootMap = lootQueue.poll();
            for (ConditionalElement loots : currentLootMap.values()) {
                if (RequirementManager.isRequirementMet(condition, loots.getRequirements())) {
                    loots.combine(player, lootWeightMap);
                    if (loots.getSubElements() != null) {
                        lootQueue.add(loots.getSubElements());
                    }
                }
            }
        }
        return lootWeightMap;
    }

    /**
     * Retrieves an array of requirements based on a configuration section.
     *
     * @param section The configuration section containing requirement definitions.
     * @param advanced A flag indicating whether to use advanced requirements.
     * @return An array of Requirement objects based on the configuration section
     */
    @NotNull
    @Override
    public Requirement[] getRequirements(ConfigurationSection section, boolean advanced) {
        List<Requirement> requirements = new ArrayList<>();
        if (section == null) {
            return requirements.toArray(new Requirement[0]);
        }
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            String typeOrName = entry.getKey();
            if (hasRequirement(typeOrName)) {
                requirements.add(getRequirement(typeOrName, entry.getValue()));
            } else {
                requirements.add(getRequirement(section.getConfigurationSection(typeOrName), advanced));
            }
        }
        return requirements.toArray(new Requirement[0]);
    }

    public boolean hasRequirement(String type) {
        return requirementBuilderMap.containsKey(type);
    }

    /**
     * Retrieves a Requirement object based on a configuration section and advanced flag.
     *
     * @param section  The configuration section containing requirement definitions.
     * @param advanced A flag indicating whether to use advanced requirements.
     * @return A Requirement object based on the configuration section, or an EmptyRequirement if the section is null or invalid.
     */
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
        var builder = getRequirementFactory(type);
        if (builder == null) {
            return EmptyRequirement.instance;
        }
        return builder.build(section.get("value"), actionList, advanced);
    }

    /**
     * Gets a requirement based on the provided key and value.
     * If a valid RequirementFactory is found for the key, it is used to create the requirement.
     * If no factory is found, a warning is logged, and an empty requirement instance is returned.
     *
     * @param type   The key representing the requirement type.
     * @param value The value associated with the requirement.
     * @return A Requirement instance based on the key and value, or an empty requirement if not found.
     */
    @Override
    @NotNull
    public Requirement getRequirement(String type, Object value) {
        RequirementFactory factory = getRequirementFactory(type);
        if (factory == null) {
            LogUtils.warn("Requirement type: " + type + " doesn't exist.");
            return EmptyRequirement.instance;
        }
        return factory.build(value);
    }

    /**
     * Retrieves a RequirementFactory based on the specified requirement type.
     *
     * @param type The requirement type for which to retrieve a factory.
     * @return A RequirementFactory for the specified type, or null if no factory is found.
     */
    @Override
    @Nullable
    public RequirementFactory getRequirementFactory(String type) {
        return requirementBuilderMap.get(type);
    }

    private void registerTimeRequirement() {
        registerRequirement("time", (args, actions, advanced) -> {
            List<Pair<Integer, Integer>> timePairs = ConfigUtils.stringListArgs(args).stream().map(it -> ConfigUtils.splitStringIntegerArgs(it, "~")).toList();
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

    @SuppressWarnings("all")
    private void registerGroupRequirement() {
        registerRequirement("group", (args, actions, advanced) -> {
            List<String> arg = (List<String>) args;
            return condition -> {
                String lootID = condition.getArg("{loot}");
                Loot loot = plugin.getLootManager().getLoot(lootID);
                String[] groups = loot.getLootGroup();
                if (groups != null) {
                    for (String g : groups) {
                        if (arg.contains(g)) {
                            return true;
                        }
                    }
                }
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!group", (args, actions, advanced) -> {
            List<String> arg = (List<String>) args;
            return condition -> {
                String lootID = condition.getArg("{loot}");
                Loot loot = plugin.getLootManager().getLoot(lootID);
                String[] groups = loot.getLootGroup();
                if (groups == null) {
                    return true;
                }
                outer: {
                    for (String g : groups) {
                        if (arg.contains(g)) {
                            break outer;
                        }
                    }
                    return true;
                }
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    @SuppressWarnings("unchecked")
    private void registerLootRequirement() {
        registerRequirement("loot", (args, actions, advanced) -> {
            List<String> arg = (List<String>) args;
            return condition -> {
                String lootID = condition.getArg("{loot}");
                if (arg.contains(lootID)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!loot", (args, actions, advanced) -> {
            List<String> arg = (List<String>) args;
            return condition -> {
                String lootID = condition.getArg("{loot}");
                if (!arg.contains(lootID)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerYRequirement() {
        registerRequirement("ypos", (args, actions, advanced) -> {
            List<Pair<Integer, Integer>> timePairs = ConfigUtils.stringListArgs(args).stream().map(it -> ConfigUtils.splitStringIntegerArgs(it, "~")).toList();
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
                return EmptyRequirement.instance;
            }
        });
    }

    private void registerAndRequirement() {
        registerRequirement("&&", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                Requirement[] requirements = getRequirements(section, advanced);
                return condition -> {
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
                return EmptyRequirement.instance;
            }
        });
    }

    private void registerLavaFishingRequirement() {
        registerRequirement("lava-fishing", (args, actions, advanced) -> {
            boolean inLava = (boolean) args;
            return condition -> {
                String current = condition.getArgs().getOrDefault("{lava}","false");
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
                String current = condition.getArgs().getOrDefault("{open-water}", "false");
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

    private void registerMoneyRequirement() {
        registerRequirement("money", (args, actions, advanced) -> {
            double money = ConfigUtils.getDoubleValue(args);
            return condition -> {
                double current = VaultHook.getEconomy().getBalance(condition.getPlayer());
                if (current >= money)
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

    private void registerCoolDownRequirement() {
        registerRequirement("cooldown", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String key = section.getString("key");
                int time = section.getInt("time");
                return condition -> {
                    if (!plugin.getCoolDownManager().isCoolDown(condition.getPlayer().getUniqueId(), key, time)) {
                        return true;
                    }
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at cooldown requirement.");
                return EmptyRequirement.instance;
            }
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

    @SuppressWarnings("DuplicatedCode")
    private void registerGreaterThanRequirement() {
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
            }
        });
    }

    private void registerRegexRequirement() {
        registerRequirement("regex", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String v1 = section.getString("papi", "");
                String v2 = section.getString("regex", "");
                return condition -> {
                    if (ParseUtils.setPlaceholders(condition.getPlayer(), v1).matches(v2)) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at regex requirement.");
                return EmptyRequirement.instance;
            }
        });
    }

    private void registerNumberEqualRequirement() {
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
            }
        });
    }

    @SuppressWarnings("DuplicatedCode")
    private void registerLessThanRequirement() {
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
            }
        });
    }

    private void registerEndWithRequirement() {
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
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
                return EmptyRequirement.instance;
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

    private void registerItemInHandRequirement() {
        registerRequirement("item-in-hand", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                boolean mainOrOff = section.getString("hand","main").equalsIgnoreCase("main");
                int amount = section.getInt("amount", 1);
                List<String> items = ConfigUtils.stringListArgs(section.get("item"));
                return condition -> {
                    ItemStack itemStack = mainOrOff ?
                            condition.getPlayer().getInventory().getItemInMainHand()
                            : condition.getPlayer().getInventory().getItemInOffHand();
                    String id = plugin.getItemManager().getAnyPluginItemID(itemStack);
                    if (items.contains(id) && itemStack.getAmount() >= amount) return true;
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at item-in-hand requirement.");
                return EmptyRequirement.instance;
            }
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
        registerRequirement("has-bait", (args, actions, advanced) -> {
            boolean has = (boolean) args;
            return condition -> {
                String id = condition.getArg("{bait}");
                if (id != null && has) return true;
                if (id == null && !has) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerSizeRequirement() {
        registerRequirement("has-size", (args, actions, advanced) -> {
            boolean has = (boolean) args;
            return condition -> {
                String size = condition.getArg("{SIZE}");
                if (size != null && has) return true;
                if (size == null && !has) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerHasStatsRequirement() {
        registerRequirement("has-stats", (args, actions, advanced) -> {
            boolean has = (boolean) args;
            return condition -> {
                String loot = condition.getArg("{loot}");
                Loot lootInstance = plugin.getLootManager().getLoot(loot);
                if (lootInstance != null) {
                    if (!lootInstance.disableStats() && has) return true;
                    if (lootInstance.disableStats() && !has) return true;
                }
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerLootTypeRequirement() {
        registerRequirement("loot-type", (args, actions, advanced) -> {
            List<String> types = ConfigUtils.stringListArgs(args);
            return condition -> {
                String loot = condition.getArg("{loot}");
                Loot lootInstance = plugin.getLootManager().getLoot(loot);
                if (lootInstance != null) {
                    if (types.contains(lootInstance.getType().name().toLowerCase(Locale.ENGLISH))) return true;
                }
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!loot-type", (args, actions, advanced) -> {
            List<String> types = ConfigUtils.stringListArgs(args);
            return condition -> {
                String loot = condition.getArg("{loot}");
                Loot lootInstance = plugin.getLootManager().getLoot(loot);
                if (lootInstance != null) {
                    if (!types.contains(lootInstance.getType().name().toLowerCase(Locale.ENGLISH))) return true;
                }
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerEnvironmentRequirement() {
        registerRequirement("environment", (args, actions, advanced) -> {
            List<String> environments = ConfigUtils.stringListArgs(args);
            return condition -> {
                var name = condition.getLocation().getWorld().getEnvironment().name().toLowerCase(Locale.ENGLISH);
                if (environments.contains(name)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!environment", (args, actions, advanced) -> {
            List<String> environments = ConfigUtils.stringListArgs(args);
            return condition -> {
                var name = condition.getLocation().getWorld().getEnvironment().name().toLowerCase(Locale.ENGLISH);
                if (!environments.contains(name)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerHookRequirement() {
        registerRequirement("hook", (args, actions, advanced) -> {
            List<String> hooks = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("{hook}");
                if (hooks.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("!hook", (args, actions, advanced) -> {
            List<String> hooks = ConfigUtils.stringListArgs(args);
            return condition -> {
                String id = condition.getArg("{hook}");
                if (!hooks.contains(id)) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
        registerRequirement("has-hook", (args, actions, advanced) -> {
            boolean has = (boolean) args;
            return condition -> {
                String id = condition.getArg("{hook}");
                if (id != null && has) return true;
                if (id == null && !has) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerInBagRequirement() {
        registerRequirement("in-fishingbag", (args, actions, advanced) -> {
            boolean arg = (boolean) args;
            return condition -> {
                String inBag = condition.getArg("{in-bag}");
                if (inBag == null && !arg) return true;
                if (inBag != null && inBag.equals(String.valueOf(arg))) return true;
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    private void registerListRequirement() {
        registerRequirement("list", (args, actions, advanced) -> {
            LogUtils.severe("It seems that you made a mistake where you put \"list\" into \"conditions\" section.");
            ArrayList<String> list = ConfigUtils.stringListArgs(args);
            LogUtils.warn("list:");
            for (String e : list) {
                LogUtils.warn(" - " + e);
            }
            return EmptyRequirement.instance;
        });
    }

    private void registerCompetitionRequirement() {
        registerRequirement("competition", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                boolean onCompetition = section.getBoolean("ongoing", true);
                List<String> ids = ConfigUtils.stringListArgs(section.get("id"));
                return condition -> {
                    if (ids.size() == 0) {
                        if (plugin.getCompetitionManager().getOnGoingCompetition() != null == onCompetition) {
                            return true;
                        }
                    } else {
                        FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                        if (onCompetition) {
                            if (competition != null) {
                                if (ids.contains(competition.getConfig().getKey())) {
                                    return true;
                                }
                            }
                        } else {
                            if (competition == null) {
                                return true;
                            }
                        }
                    }
                    if (advanced) triggerActions(actions, condition);
                    return false;
                };
            } else {
                LogUtils.warn("Wrong value format found at competition requirement.");
                return EmptyRequirement.instance;
            }
        });
    }

    private void registerPluginLevelRequirement() {
        registerRequirement("plugin-level", (args, actions, advanced) -> {
            if (args instanceof ConfigurationSection section) {
                String pluginName = section.getString("plugin");
                int level = section.getInt("level");
                String target = section.getString("target");
                return condition -> {
                    LevelInterface levelInterface = plugin.getIntegrationManager().getLevelPlugin(pluginName);
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
                return EmptyRequirement.instance;
            }
        });
    }


    private void registerPotionEffectRequirement() {
        registerRequirement("potion-effect", (args, actions, advanced) -> {
            String potions = (String) args;
            String[] split = potions.split("(<=|>=|<|>|==)", 2);
            PotionEffectType type = PotionEffectType.getByName(split[0]);
            if (type == null) {
                LogUtils.warn("Potion effect doesn't exist: " + split[0]);
                return EmptyRequirement.instance;
            }
            int required = Integer.parseInt(split[1]);
            String operator = potions.substring(split[0].length(), potions.length() - split[1].length());
            return condition -> {
                int level = -1;
                PotionEffect potionEffect = condition.getPlayer().getPotionEffect(type);
                if (potionEffect != null) {
                    level = potionEffect.getAmplifier();
                }
                boolean result = false;
                switch (operator) {
                    case ">=" -> {
                        if (level >= required) result = true;
                    }
                    case ">" -> {
                        if (level > required) result = true;
                    }
                    case "==" -> {
                        if (level == required) result = true;
                    }
                    case "!=" -> {
                        if (level != required) result = true;
                    }
                    case "<=" -> {
                        if (level <= required) result = true;
                    }
                    case "<" -> {
                        if (level < required) result = true;
                    }
                }
                if (result) {
                    return true;
                }
                if (advanced) triggerActions(actions, condition);
                return false;
            };
        });
    }

    /**
     * Triggers a list of actions with the given condition.
     * If the list of actions is not null, each action in the list is triggered.
     *
     * @param actions   The list of actions to trigger.
     * @param condition The condition associated with the actions.
     */
    private void triggerActions(List<Action> actions, Condition condition) {
        if (actions != null)
            for (Action action : actions)
                action.trigger(condition);
    }

    /**
     * Loads requirement expansions from external JAR files located in the expansion folder.
     * Each expansion JAR should contain classes that extends the RequirementExpansion class.
     * Expansions are registered and used to create custom requirements.
     * If an error occurs while loading or initializing an expansion, a warning message is logged.
     */
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
