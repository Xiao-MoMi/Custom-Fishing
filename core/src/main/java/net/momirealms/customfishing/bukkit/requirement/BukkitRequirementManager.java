package net.momirealms.customfishing.bukkit.requirement;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.EffectProperties;
import net.momirealms.customfishing.api.mechanic.requirement.*;
import net.momirealms.customfishing.common.util.ClassUtils;
import net.momirealms.customfishing.common.util.ListUtils;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
        this.loadExpansions();
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
    public Requirement<Player>[] parseRequirements(@NotNull Section section, boolean runActions) {
        List<Requirement<Player>> requirements = new ArrayList<>();
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
            return EmptyRequirement.INSTANCE;
        }
        var factory = getRequirementFactory(type);
        if (factory == null) {
            plugin.getPluginLogger().warn("No requirement type found at " + section.getRouteAsString());
            return EmptyRequirement.INSTANCE;
        }
        return factory.process(section.get("value"), actionList, runActions);
    }

    @NotNull
    @Override
    public Requirement<Player> parseRequirement(@NotNull String type, @NotNull Object value) {
        RequirementFactory<Player> factory = getRequirementFactory(type);
        if (factory == null) {
            plugin.getPluginLogger().warn("Requirement type: " + type + " doesn't exist.");
            return EmptyRequirement.INSTANCE;
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
    }

    private void registerTimeRequirement() {
        registerRequirement("time", (args, actions, advanced) -> {
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
                if (advanced) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerYRequirement() {
        registerRequirement("ypos", (args, actions, advanced) -> {
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
                if (advanced) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerOrRequirement() {
        registerRequirement("||", (args, actions, advanced) -> {
            if (args instanceof Section section) {
                Requirement<Player>[] requirements = parseRequirements(section, advanced);
                return context -> {
                    for (Requirement<Player> requirement : requirements)
                        if (requirement.isSatisfied(context))
                            return true;
                    if (advanced) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at || requirement which should be Section");
                return EmptyRequirement.INSTANCE;
            }
        });
    }

    private void registerAndRequirement() {
        registerRequirement("&&", (args, actions, advanced) -> {
            if (args instanceof Section section) {
                Requirement<Player>[] requirements = parseRequirements(section, advanced);
                return context -> {
                    outer: {
                        for (Requirement<Player> requirement : requirements)
                            if (!requirement.isSatisfied(context))
                                break outer;
                        return true;
                    }
                    if (advanced) ActionManager.trigger(context, actions);
                    return false;
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at && requirement which should be Section");
                return EmptyRequirement.INSTANCE;
            }
        });
    }

    private void registerInWaterRequirement() {
        registerRequirement("in-water", (args, actions, advanced) -> {
            boolean inWater = (boolean) args;
            return context -> {
                boolean in_water = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.WATER_FISHING.key());
                if (in_water == inWater) return true;
                if (advanced) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerInVoidRequirement() {
        registerRequirement("in-void", (args, actions, advanced) -> {
            boolean inWater = (boolean) args;
            return context -> {
                boolean in_water = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.VOID_FISHING.key());
                if (in_water == inWater) return true;
                if (advanced) ActionManager.trigger(context, actions);
                return false;
            };
        });
    }

    private void registerInLavaRequirement() {
        registerRequirement("lava-fishing", (args, actions, advanced) -> {
            boolean inLava = (boolean) args;
            if (!inLava) {
                throw new IllegalArgumentException("");
            }
            return context -> {
                boolean in_lava = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.LAVA_FISHING.key());
                if (in_lava) return true;
                if (advanced) ActionManager.trigger(context, actions);
                return false;
            };
        });
        registerRequirement("in-lava", (args, actions, advanced) -> {
            boolean inLava = (boolean) args;
            return context -> {
                boolean in_lava = Optional.ofNullable(context.arg(ContextKeys.SURROUNDING)).orElse("").equals(EffectProperties.LAVA_FISHING.key());
                if (in_lava == inLava) return true;
                if (advanced) ActionManager.trigger(context, actions);
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