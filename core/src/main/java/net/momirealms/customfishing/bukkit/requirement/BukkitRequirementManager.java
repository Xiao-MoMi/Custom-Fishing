/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.requirement;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.integration.LevelerProvider;
import net.momirealms.customfishing.api.integration.SeasonProvider;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.EffectProperties;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.misc.season.Season;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementExpansion;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementFactory;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.util.MoonPhase;
import net.momirealms.customfishing.bukkit.integration.VaultHook;
import net.momirealms.customfishing.common.util.ClassUtils;
import net.momirealms.customfishing.common.util.ListUtils;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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

import static java.util.Objects.requireNonNull;

public class BukkitRequirementManager implements RequirementManager<Player> {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, RequirementFactory<Player>> requirementFactoryMap = new HashMap<>();
    private static final String EXPANSION_FOLDER = "expansions/requirement";

    public BukkitRequirementManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.registerBuiltInRequirements();
    }

    @Override
    public void reload() {
        this.loadExpansions();
    }

    @Override
    public void disable() {
        this.requirementFactoryMap.clear();
    }

    @Override
    public boolean registerRequirement(@NotNull String type, @NotNull RequirementFactory<Player> requirementFactory) {
        if (this.requirementFactoryMap.containsKey(type)) return false;
        this.requirementFactoryMap.put(type, requirementFactory);
        return true;
    }

    @Override
    public boolean unregisterRequirement(@NotNull String type) {
        return this.requirementFactoryMap.remove(type) != null;
    }

    @Nullable
    @Override
    public RequirementFactory<Player> getRequirementFactory(@NotNull String type) {
        return requirementFactoryMap.get(type);
    }

    @Override
    public boolean hasRequirement(@NotNull String type) {
        return requirementFactoryMap.containsKey(type);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Requirement<Player>[] parseRequirements(Section section, boolean runActions) {
        List<Requirement<Player>> requirements = new ArrayList<>();
        if (section != null)
            for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                String typeOrName = entry.getKey();
                if (hasRequirement(typeOrName)) {
                    requirements.add(parseRequirement(typeOrName, entry.getValue()));
                } else {
                    requirements.add(parseRequirement(section.getSection(typeOrName), runActions));
                }
            }
        return requirements.toArray(new Requirement[0]);
    }

    @NotNull
    @Override
    public Requirement<Player> parseRequirement(@NotNull Section section, boolean runActions) {
        List<Action<Player>> actionList = new ArrayList<>();
        if (runActions && section.contains("not-met-actions")) {
            actionList.addAll(List.of(plugin.getActionManager().parseActions(requireNonNull(section.getSection("not-met-actions")))));
        }
        String type = section.getString("type");
        if (type == null) {
            plugin.getPluginLogger().warn("No requirement type found at " + section.getRouteAsString());
            return Requirement.empty();
        }
        var factory = getRequirementFactory(type);
        if (factory == null) {
            plugin.getPluginLogger().warn("Requirement type: " + type + " not exists");
            return Requirement.empty();
        }
        return factory.process(section.get("value"), actionList, runActions);
    }

    @NotNull
    @Override
    public Requirement<Player> parseRequirement(@NotNull String type, @NotNull Object value) {
        RequirementFactory<Player> factory = getRequirementFactory(type);
        if (factory == null) {
            plugin.getPluginLogger().warn("Requirement type: " + type + " doesn't exist.");
            return Requirement.empty();
        }
        return factory.process(value);
    }

    private void registerBuiltInRequirements() {
        this.registerTimeRequirement();
        this.registerYRequirement();
        this.registerInWaterRequirement();
        this.registerInVoidRequirement();
        this.registerInLavaRequirement();
        this.registerAndRequirement();
        this.registerOrRequirement();
        this.registerGroupRequirement();
        this.registerRodRequirement();
        this.registerPAPIRequirement();
        this.registerSeasonRequirement();
        this.registerPermissionRequirement();
        this.registerMoonPhaseRequirement();
        this.registerCoolDownRequirement();
        this.registerDateRequirement();
        this.registerWeatherRequirement();
        this.registerBiomeRequirement();
        this.registerWorldRequirement();
        this.registerMoneyRequirement();
        this.registerLevelRequirement();
        this.registerRandomRequirement();
        this.registerIceFishingRequirement();
        this.registerOpenWaterRequirement();
        this.registerBaitRequirement();
        this.registerLootRequirement();
        this.registerSizeRequirement();
        this.registerLootTypeRequirement();
        this.registerHasStatsRequirement();
        this.registerHookRequirement();
        this.registerEnvironmentRequirement();
        this.registerListRequirement();
        this.registerInBagRequirement();
        this.registerCompetitionRequirement();
        this.registerPluginLevelRequirement();
        this.registerItemInHandRequirement();
        this.registerImpossibleRequirement();
        this.registerPotionEffectRequirement();
    }

    private void registerImpossibleRequirement() {
        registerRequirement("impossible", ((args, actions, runActions) -> context -> {
            if (runActions) ActionManager.trigger(context, actions);
            return false;
        }));
    }

    private void registerCompetitionRequirement() {
        registerRequirement("competition", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                boolean onCompetition = section.getBoolean("ongoing", true);
                List<String> ids = section.contains("id") ? ListUtils.toList(section.get("id")) : List.of();
                return context -> {
                    if (ids.isEmpty()) {
                        if (plugin.getCompetitionManager().getOnGoingCompetition() != null == onCompetition) {
                            return true;
                        }
                    } else {
                        FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                        if (onCompetition) {
                            if (competition != null)
                                if (ids.contains(competition.getConfig().id()))
                                    return true;
                        } else {
                            if (competition == null)
                                return true;
                        }
                    }
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at competition requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
    }

    private void registerInBagRequirement() {
        registerRequirement("in-fishingbag", (args, actions, runActions) -> {
            boolean arg = (boolean) args;
            return context -> {
                boolean inBag = Optional.ofNullable(context.arg(ContextKeys.IN_BAG)).orElse(false);
                if (inBag == arg) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerItemInHandRequirement() {
        registerRequirement("item-in-hand", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                boolean mainOrOff = section.getString("hand","main").equalsIgnoreCase("main");
                int amount = section.getInt("amount", 1);
                List<String> items = ListUtils.toList(section.get("item"));
                return context -> {
                    ItemStack itemStack = mainOrOff ?
                            context.getHolder().getInventory().getItemInMainHand()
                            : context.getHolder().getInventory().getItemInOffHand();
                    String id = plugin.getItemManager().getItemID(itemStack);
                    if (items.contains(id) && itemStack.getAmount() >= amount) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at item-in-hand requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
    }

    private void registerPluginLevelRequirement() {
        registerRequirement("plugin-level", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                String pluginName = section.getString("plugin");
                int level = section.getInt("level");
                String target = section.getString("target");
                return context -> {
                    LevelerProvider levelerProvider = plugin.getIntegrationManager().getLevelerProvider(pluginName);
                    if (levelerProvider == null) {
                        plugin.getPluginLogger().warn("Plugin (" + pluginName + "'s) level is not compatible. Please double check if it's a problem caused by pronunciation.");
                        return true;
                    }
                    if (levelerProvider.getLevel(context.getHolder(), target) >= level)
                        return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at plugin-level requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
    }

    private void registerTimeRequirement() {
        registerRequirement("time", (args, actions, runActions) -> {
            List<String> list = ListUtils.toList(args);
            List<Pair<Integer, Integer>> timePairs = list.stream().map(line -> {
                String[] split = line.split("~");
                return new Pair<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            }).toList();
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                long time = location.getWorld().getTime();
                for (Pair<Integer, Integer> pair : timePairs)
                    if (time >= pair.left() && time <= pair.right())
                        return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerYRequirement() {
        registerRequirement("ypos", (args, actions, runActions) -> {
            List<String> list = ListUtils.toList(args);
            List<Pair<Double, Double>> posPairs = list.stream().map(line -> {
                String[] split = line.split("~");
                return new Pair<>(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
            }).toList();
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                double y = location.getY();
                for (Pair<Double, Double> pair : posPairs)
                    if (y >= pair.left() && y <= pair.right())
                        return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerOrRequirement() {
        registerRequirement("||", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                Requirement<Player>[] requirements = parseRequirements(section, runActions);
                return context -> {
                    for (Requirement<Player> requirement : requirements)
                        if (requirement.isSatisfied(context))
                            return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at || requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
    }

    private void registerAndRequirement() {
        registerRequirement("&&", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                Requirement<Player>[] requirements = parseRequirements(section, runActions);
                return context -> {
                    outer: {
                        for (Requirement<Player> requirement : requirements)
                            if (!requirement.isSatisfied(context))
                                break outer;
                        return true;
                    }
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at && requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
    }

    private void registerInWaterRequirement() {
        registerRequirement("in-water", (args, actions, runActions) -> {
            boolean inWater = (boolean) args;
            return context -> {
                boolean in_water = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.WATER_FISHING.key());
                if (in_water == inWater) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerInVoidRequirement() {
        registerRequirement("in-void", (args, actions, runActions) -> {
            boolean inVoid = (boolean) args;
            return context -> {
                boolean in_void = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.VOID_FISHING.key());
                if (in_void == inVoid) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerInLavaRequirement() {
        // Deprecated requirement type
        registerRequirement("lava-fishing", (args, actions, runActions) -> {
            boolean inLava = (boolean) args;
            if (!inLava) {
                // in water
                return context -> {
                    boolean in_water = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.WATER_FISHING.key());
                    if (in_water) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            }
            // in lava
            return context -> {
                boolean in_lava = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.LAVA_FISHING.key());
                if (in_lava) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("in-lava", (args, actions, runActions) -> {
            boolean inLava = (boolean) args;
            return context -> {
                boolean in_lava = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.LAVA_FISHING.key());
                if (in_lava == inLava) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerRodRequirement() {
        registerRequirement("rod", (args, actions, runActions) -> {
            HashSet<String> rods = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String id = context.arg(ContextKeys.ROD);
                if (rods.contains(id)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!rod", (args, actions, runActions) -> {
            HashSet<String> rods = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String id = context.arg(ContextKeys.ROD);
                if (!rods.contains(id)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerGroupRequirement() {
        registerRequirement("group", (args, actions, runActions) -> {
            HashSet<String> groups = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String lootID = context.arg(ContextKeys.ID);
                Optional<Loot> loot = plugin.getLootManager().getLoot(lootID);
                if (loot.isEmpty()) return false;
                String[] group = loot.get().lootGroup();
                if (group != null)
                    for (String x : group)
                        if (groups.contains(x))
                            return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!group", (args, actions, runActions) -> {
            HashSet<String> groups = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String lootID = context.arg(ContextKeys.ID);
                Optional<Loot> loot = plugin.getLootManager().getLoot(lootID);
                if (loot.isEmpty()) return false;
                String[] group = loot.get().lootGroup();
                if (group == null)
                    return true;
                outer: {
                    for (String x : group)
                        if (groups.contains(x))
                            break outer;
                    return true;
                }
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerLootRequirement() {
        registerRequirement("loot", (args, actions, runActions) -> {
            HashSet<String> arg = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String lootID = context.arg(ContextKeys.ID);
                if (arg.contains(lootID)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!loot", (args, actions, runActions) -> {
            HashSet<String> arg = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String lootID = context.arg(ContextKeys.ID);
                if (!arg.contains(lootID)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerHookRequirement() {
        registerRequirement("hook", (args, actions, runActions) -> {
            HashSet<String> hooks = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String id = context.arg(ContextKeys.HOOK);
                if (hooks.contains(id)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!hook", (args, actions, runActions) -> {
            HashSet<String> hooks = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String id = context.arg(ContextKeys.HOOK);
                if (!hooks.contains(id)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("has-hook", (args, actions, runActions) -> {
            boolean has = (boolean) args;
            return context -> {
                String id = context.arg(ContextKeys.HOOK);
                if (id != null && has) return true;
                if (id == null && !has) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerBaitRequirement() {
        registerRequirement("bait", (args, actions, runActions) -> {
            HashSet<String> arg = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String id = context.arg(ContextKeys.BAIT);
                if (arg.contains(id)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!bait", (args, actions, runActions) -> {
            HashSet<String> arg = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String id = context.arg(ContextKeys.BAIT);
                if (!arg.contains(id)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("has-bait", (args, actions, runActions) -> {
            boolean has = (boolean) args;
            return context -> {
                String id = context.arg(ContextKeys.BAIT);
                if (id != null && has) return true;
                if (id == null && !has) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerSizeRequirement() {
        registerRequirement("has-size", (args, actions, runActions) -> {
            boolean has = (boolean) args;
            return context -> {
                float size = Optional.ofNullable(context.arg(ContextKeys.SIZE)).orElse(-1f);
                if (size != -1 && has) return true;
                if (size == -1 && !has) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerOpenWaterRequirement() {
        registerRequirement("open-water", (args, actions, runActions) -> {
            boolean openWater = (boolean) args;
            return context -> {
                boolean current = Optional.ofNullable(context.arg(ContextKeys.OPEN_WATER)).orElse(false);
                if (openWater == current)
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerHasStatsRequirement() {
        registerRequirement("has-stats", (args, actions, runActions) -> {
            boolean has = (boolean) args;
            return context -> {
                String loot = context.arg(ContextKeys.ID);
                Optional<Loot> lootInstance = plugin.getLootManager().getLoot(loot);
                if (lootInstance.isPresent()) {
                    if (!lootInstance.get().disableStats() && has) return true;
                    if (lootInstance.get().disableStats() && !has) return true;
                }
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerLootTypeRequirement() {
        registerRequirement("loot-type", (args, actions, runActions) -> {
            List<String> types = ListUtils.toList(args);
            return context -> {
                String loot = context.arg(ContextKeys.ID);
                Optional<Loot> lootInstance = plugin.getLootManager().getLoot(loot);
                if (lootInstance.isPresent()) {
                    if (types.contains(lootInstance.get().type().name().toLowerCase(Locale.ENGLISH)))
                        return true;
                }
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!loot-type", (args, actions, runActions) -> {
            List<String> types = ListUtils.toList(args);
            return context -> {
                String loot = context.arg(ContextKeys.ID);
                Optional<Loot> lootInstance = plugin.getLootManager().getLoot(loot);
                if (lootInstance.isPresent()) {
                    if (!types.contains(lootInstance.get().type().name().toLowerCase(Locale.ENGLISH)))
                        return true;
                }
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerListRequirement() {
        registerRequirement("list", (args, actions, runActions) -> {
            plugin.getPluginLogger().severe("It seems that you made a mistake where you put \"list\" into \"conditions\" section.");
            plugin.getPluginLogger().warn("list:");
            for (String e : ListUtils.toList(args)) {
                plugin.getPluginLogger().warn(" - " + e);
            }
            return Requirement.empty();
        });
    }

    private void registerEnvironmentRequirement() {
        registerRequirement("environment", (args, actions, runActions) -> {
            List<String> environments = ListUtils.toList(args);
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                var name = location.getWorld().getEnvironment().name().toLowerCase(Locale.ENGLISH);
                if (environments.contains(name)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!environment", (args, actions, runActions) -> {
            List<String> environments = ListUtils.toList(args);
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                var name = location.getWorld().getEnvironment().name().toLowerCase(Locale.ENGLISH);
                if (!environments.contains(name)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerIceFishingRequirement() {
        registerRequirement("ice-fishing", (args, actions, runActions) -> {
            boolean iceFishing = (boolean) args;
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.OTHER_LOCATION));
                int water = 0, ice = 0;
                for (int i = -2; i <= 2; i++)
                    for (int j = -1; j <= 2; j++)
                        for (int k = -2; k <= 2; k++) {
                            Block block = location.clone().add(i, j, k).getBlock();
                            Material material = block.getType();
                            switch (material) {
                                case ICE -> ice++;
                                case WATER -> water++;
                            }
                        }
                if ((ice >= 16 && water >= 25) == iceFishing)
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerLevelRequirement() {
        registerRequirement("level", (args, actions, runActions) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                int current = context.getHolder().getLevel();
                if (current >= value.evaluate(context, true))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerMoneyRequirement() {
        registerRequirement("money", (args, actions, runActions) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                double current = VaultHook.getBalance(context.getHolder());
                if (current >= value.evaluate(context, true))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerRandomRequirement() {
        registerRequirement("random", (args, actions, runActions) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() < value.evaluate(context, true))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerBiomeRequirement() {
        registerRequirement("biome", (args, actions, runActions) -> {
            HashSet<String> biomes = new HashSet<>(ListUtils.toList(args));
            return context -> {
                Location location = requireNonNull(Optional.ofNullable(context.arg(ContextKeys.OTHER_LOCATION)).orElse(context.arg(ContextKeys.LOCATION)));
                String currentBiome = SparrowHeart.getInstance().getBiomeResourceLocation(location);
                if (biomes.contains(currentBiome))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!biome", (args, actions, runActions) -> {
            HashSet<String> biomes = new HashSet<>(ListUtils.toList(args));
            return context -> {
                Location location = requireNonNull(Optional.ofNullable(context.arg(ContextKeys.OTHER_LOCATION)).orElse(context.arg(ContextKeys.LOCATION)));
                String currentBiome = SparrowHeart.getInstance().getBiomeResourceLocation(location);
                if (!biomes.contains(currentBiome))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerMoonPhaseRequirement() {
        registerRequirement("moon-phase", (args, actions, runActions) -> {
            HashSet<String> moonPhases = new HashSet<>(ListUtils.toList(args));
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                long days = location.getWorld().getFullTime() / 24_000;
                if (moonPhases.contains(MoonPhase.getPhase(days).name().toLowerCase(Locale.ENGLISH)))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!moon-phase", (args, actions, runActions) -> {
            HashSet<String> moonPhases = new HashSet<>(ListUtils.toList(args));
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                long days = location.getWorld().getFullTime() / 24_000;
                if (!moonPhases.contains(MoonPhase.getPhase(days).name().toLowerCase(Locale.ENGLISH)))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerWorldRequirement() {
        registerRequirement("world", (args, actions, runActions) -> {
            HashSet<String> worlds = new HashSet<>(ListUtils.toList(args));
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                if (worlds.contains(location.getWorld().getName()))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!world", (args, actions, runActions) -> {
            HashSet<String> worlds = new HashSet<>(ListUtils.toList(args));
            return context -> {
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                if (!worlds.contains(location.getWorld().getName()))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerWeatherRequirement() {
        registerRequirement("weather", (args, actions, runActions) -> {
            HashSet<String> weathers = new HashSet<>(ListUtils.toList(args));
            return context -> {
                String currentWeather;
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                World world = location.getWorld();
                if (world.isClearWeather()) currentWeather = "clear";
                else if (world.isThundering()) currentWeather = "thunder";
                else currentWeather = "rain";
                if (weathers.contains(currentWeather)) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerCoolDownRequirement() {
        registerRequirement("cooldown", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                String key = section.getString("key");
                int time = section.getInt("time");
                return context -> {
                    if (!plugin.getCoolDownManager().isCoolDown(context.getHolder().getUniqueId(), key, time))
                        return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at cooldown requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
    }

    private void registerDateRequirement() {
        registerRequirement("date", (args, actions, runActions) -> {
            HashSet<String> dates = new HashSet<>(ListUtils.toList(args));
            return context -> {
                Calendar calendar = Calendar.getInstance();
                String current = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE);
                if (dates.contains(current))
                    return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerPermissionRequirement() {
        registerRequirement("permission", (args, actions, runActions) -> {
            List<String> perms = ListUtils.toList(args);
            return context -> {
                for (String perm : perms)
                    if (context.getHolder().hasPermission(perm))
                        return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("!permission", (args, actions, runActions) -> {
            List<String> perms = ListUtils.toList(args);
            return context -> {
                for (String perm : perms)
                    if (context.getHolder().hasPermission(perm)) {
                        if (runActions) ActionManager.trigger(context, actions);
                        return false;
                    }
                return true;
            };
        });
    }

    private void registerSeasonRequirement() {
        registerRequirement("season", (args, actions, runActions) -> {
            List<String> seasons = ListUtils.toList(args);
            return context -> {
                SeasonProvider seasonProvider = plugin.getIntegrationManager().getSeasonProvider();
                if (seasonProvider == null) return true;
                Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                World world = location.getWorld();
                Season season = seasonProvider.getSeason(world);
                if (seasons.contains(season.name().toLowerCase(Locale.ENGLISH))) return true;
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerPAPIRequirement() {
        registerRequirement("<", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                MathValue<Player> v1 = MathValue.auto(section.get("value1"));
                MathValue<Player> v2 = MathValue.auto(section.get("value2"));
                return context -> {
                    if (v1.evaluate(context, true) < v2.evaluate(context, true)) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at < requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("<=", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                MathValue<Player> v1 = MathValue.auto(section.get("value1"));
                MathValue<Player> v2 = MathValue.auto(section.get("value2"));
                return context -> {
                    if (v1.evaluate(context, true) <= v2.evaluate(context, true)) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at <= requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("!=", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                MathValue<Player> v1 = MathValue.auto(section.get("value1"));
                MathValue<Player> v2 = MathValue.auto(section.get("value2"));
                return context -> {
                    if (v1.evaluate(context, true) != v2.evaluate(context, true)) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at != requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("==", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                MathValue<Player> v1 = MathValue.auto(section.get("value1"));
                MathValue<Player> v2 = MathValue.auto(section.get("value2"));
                return context -> {
                    if (v1.evaluate(context, true) == v2.evaluate(context, true)) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at == requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement(">=", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                MathValue<Player> v1 = MathValue.auto(section.get("value1"));
                MathValue<Player> v2 = MathValue.auto(section.get("value2"));
                return context -> {
                    if (v1.evaluate(context, true) >= v2.evaluate(context, true)) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at >= requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement(">", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                MathValue<Player> v1 = MathValue.auto(section.get("value1"));
                MathValue<Player> v2 = MathValue.auto(section.get("value2"));
                return context -> {
                    if (v1.evaluate(context, true) > v2.evaluate(context, true)) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at > requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("regex", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("papi", ""));
                String v2 = section.getString("regex", "");
                return context -> {
                    if (v1.render(context, true).matches(v2)) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at regex requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("startsWith", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));
                return context -> {
                    if (v1.render(context, true).startsWith(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at startsWith requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("!startsWith", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));
                return context -> {
                    if (!v1.render(context, true).startsWith(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at !startsWith requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("endsWith", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));
                return context -> {
                    if (v1.render(context, true).endsWith(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at endsWith requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("!endsWith", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));
                return context -> {
                    if (!v1.render(context, true).endsWith(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at !endsWith requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("contains", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));
                return context -> {
                    if (v1.render(context, true).contains(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at contains requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("!contains", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));
                return context -> {
                    if (!v1.render(context, true).contains(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at !contains requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("in-list", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> papi = TextValue.auto(section.getString("papi", ""));
                List<String> values = ListUtils.toList(section.get("values"));
                return context -> {
                    if (values.contains(papi.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at in-list requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("!in-list", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> papi = TextValue.auto(section.getString("papi", ""));
                List<String> values = ListUtils.toList(section.get("values"));
                return context -> {
                    if (!values.contains(papi.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at !in-list requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("equals", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));

                return context -> {
                    if (v1.render(context, true).equals(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at equals requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
        registerRequirement("!equals", (args, actions, runActions) -> {
            if (args instanceof Section section) {
                TextValue<Player> v1 = TextValue.auto(section.getString("value1", ""));
                TextValue<Player> v2 = TextValue.auto(section.getString("value2", ""));
                return context -> {
                    if (!v1.render(context, true).equals(v2.render(context, true))) return true;
                    if (runActions) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at !equals requirement which is expected be `Section`");
                return Requirement.empty();
            }
        });
    }

    private void registerPotionEffectRequirement() {
        registerRequirement("potion-effect", (args, actions, runActions) -> {
            String potions = (String) args;
            String[] split = potions.split("(<=|>=|<|>|==)", 2);
            PotionEffectType type = PotionEffectType.getByName(split[0]);
            if (type == null) {
                plugin.getPluginLogger().warn("Potion effect doesn't exist: " + split[0]);
                return Requirement.empty();
            }
            int required = Integer.parseInt(split[1]);
            String operator = potions.substring(split[0].length(), potions.length() - split[1].length());
            return context -> {
                int level = -1;
                PotionEffect potionEffect = context.getHolder().getPotionEffect(type);
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
                if (runActions) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    /**
     * Loads requirement expansions from external JAR files located in the expansion folder.
     * Each expansion JAR should contain classes that extends the RequirementExpansion class.
     * Expansions are registered and used to create custom requirements.
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    private void loadExpansions() {
        File expansionFolder = new File(plugin.getDataFolder(), EXPANSION_FOLDER);
        if (!expansionFolder.exists())
            expansionFolder.mkdirs();
        List<Class<? extends RequirementExpansion<Player>>> classes = new ArrayList<>();
        File[] expansionJars = expansionFolder.listFiles();
        if (expansionJars == null) return;
        for (File expansionJar : expansionJars) {
            if (expansionJar.getName().endsWith(".jar")) {
                try {
                    Class<? extends RequirementExpansion<Player>> expansionClass = (Class<? extends RequirementExpansion<Player>>) ClassUtils.findClass(expansionJar, RequirementExpansion.class);
                    classes.add(expansionClass);
                } catch (IOException | ClassNotFoundException e) {
                    plugin.getPluginLogger().warn("Failed to load expansion: " + expansionJar.getName(), e);
                }
            }
        }
        try {
            for (Class<? extends RequirementExpansion<Player>> expansionClass : classes) {
                RequirementExpansion<Player> expansion = expansionClass.getDeclaredConstructor().newInstance();
                unregisterRequirement(expansion.getRequirementType());
                registerRequirement(expansion.getRequirementType(), expansion.getRequirementFactory());
                plugin.getPluginLogger().info("Loaded requirement expansion: " + expansion.getRequirementType() + "[" + expansion.getVersion() + "]" + " by " + expansion.getAuthor());
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            plugin.getPluginLogger().warn("Error occurred when creating expansion instance.", e);
        }
    }
}