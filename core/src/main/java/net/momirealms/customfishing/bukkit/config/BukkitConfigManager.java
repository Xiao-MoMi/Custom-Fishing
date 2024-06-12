package net.momirealms.customfishing.bukkit.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.config.ConfigType;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectProperties;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsKeys;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.util.ListUtils;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.customfishing.common.util.RandomUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class BukkitConfigManager extends ConfigManager {

    private static YamlDocument MAIN_CONFIG;

    public static YamlDocument getMainConfig() {
        return MAIN_CONFIG;
    }

    public BukkitConfigManager(BukkitCustomFishingPlugin plugin) {
        super(plugin);
        this.registerBuiltInItemProperties();
        this.registerBuiltInBaseEffectParser();
        this.registerBuiltInLootParser();
        this.registerBuiltInEntityParser();
        this.registerBuiltInEventParser();
        this.registerBuiltInEffectModifierParser();
    }

    @Override
    public void load() {
        MAIN_CONFIG = loadConfig("config.yml");
        this.loadSettings();
        this.loadConfigs();
    }

    private void loadSettings() {
        YamlDocument config = getMainConfig();

        metrics = config.getBoolean("metrics", true);
        checkUpdate = config.getBoolean("update-checker", true);
        debug = config.getBoolean("debug", false);

        overrideVanillaWaitTime = config.getBoolean("mechanics.fishing-wait-time.override-vanilla", false);
        waterMinTime = config.getInt("mechanics.fishing-wait-time.min-wait-time", 100);
        waterMaxTime = config.getInt("mechanics.fishing-wait-time.max-wait-time", 600);

        lavaMinTime = config.getInt("mechanics.lava-fishing.min-wait-time", 100);
        lavaMaxTime = config.getInt("mechanics.lava-fishing.max-wait-time", 600);

        restrictedSizeRange = config.getBoolean("mechanics.size.restricted-size-range", true);

        placeholderLimit = config.getInt("mechanics.competition.placeholder-limit", 3);
        serverGroup = config.getString("mechanics.competition.server-group", "default");
        redisRanking = config.getBoolean("mechanics.competition.redis-ranking", false);

        AdventureHelper.legacySupport = config.getBoolean("other-settings.legacy-color-code-support", true);
        dataSaveInterval = config.getInt("other-settings.data-save-interval", 600);
        logDataSaving = config.getBoolean("other-settings.log-data-saving", true);
        lockData = config.getBoolean("other-settings.lock-data", true);

        durabilityLore = new ArrayList<>(config.getStringList("other-settings.custom-durability-format"));

        itemDetectOrder = config.getStringList("other-settings.item-detection-order").toArray(new String[0]);
        blockDetectOrder = config.getStringList("other-settings.block-detection-order").toArray(new String[0]);

        allowMultipleTotemType = config.getBoolean("mechanics.totem.allow-multiple-type", true);
        allowSameTotemType = config.getBoolean("mechanics.totem.allow-same-type", false);

        Section placeholderSection = config.getSection("other-settings.placeholder-register");
        if (placeholderSection != null) {
            for (Map.Entry<String, Object> entry : placeholderSection.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof String original) {
                    plugin.getPlaceholderManager().registerCustomPlaceholder(entry.getKey(), original);
                }
            }
        }
    }

    private void loadConfigs() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (ConfigType type : ConfigType.values()) {
            File typeFolder = new File(plugin.getDataFolder(), "contents" + File.separator + type.path());
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.getBoostrap().saveResource("contents" + File.separator + type.path() + File.separator + "default.yml", false);
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
                        YamlDocument document = plugin.getConfigManager().loadData(subFile);
                        for (Map.Entry<String, Object> entry : document.getStringRouteMappedValues(false).entrySet()) {
                            if (entry.getValue() instanceof Section section) {
                                type.parse(entry.getKey(), section, formatFunctions);
                            }
                        }
                    }
                }
            }
        }
    }

    private void registerBuiltInItemProperties() {
        this.registerItemParser(arg -> {
            MathValue<Player> mathValue = MathValue.auto(arg);
            return (item, context) -> item.customModelData((int) mathValue.evaluate(context));
        }, 5000, "custom-model-data");
        this.registerItemParser(arg -> {
            TextValue<Player> textValue = TextValue.auto("<!i><white>" + arg);
            return (item, context) -> {
                item.displayName(AdventureHelper.miniMessageToJson(textValue.render(context)));
            };
        }, 4000, "display", "name");
        this.registerItemParser(arg -> {
            List<String> list = ListUtils.toList(arg);
            List<TextValue<Player>> lore = new ArrayList<>();
            for (String text : list) {
                lore.add(TextValue.auto("<!i><white>" + text));
            }
            return (item, context) -> {
                item.lore(lore.stream()
                        .map(it -> AdventureHelper.miniMessageToJson(it.render(context)))
                        .toList());
            };
        }, 3_000, "display", "lore");
        this.registerItemParser(arg -> {
            boolean enable = (boolean) arg;
            return (item, context) -> {
                if (!enable) return;
                item.setTag(context.arg(ContextKeys.ID), "CustomFishing", "id");
            };
        }, 2_000, "tag");
        this.registerItemParser(arg -> {
            String sizePair = (String) arg;
            String[] split = sizePair.split("~", 2);
            MathValue<Player> min = MathValue.auto(split[0]);
            MathValue<Player> max = MathValue.auto(split[1]);
            return (item, context) -> {
                double minSize = min.evaluate(context);
                double maxSize = max.evaluate(context);
                float size = (float) RandomUtils.generateRandomDouble(minSize, maxSize);
                item.setTag(size, "CustomFishing", "size");
                context.arg(ContextKeys.SIZE, size);
                context.arg(ContextKeys.SIZE_FORMATTED, String.format("%.2f", size));
            };
        }, 1_000, "size");
        this.registerItemParser(arg -> {
            Section section = (Section) arg;
            MathValue<Player> base = MathValue.auto(section.get("base", "0"));
            MathValue<Player> bonus = MathValue.auto(section.get("bonus", "0"));
            return (item, context) -> {
                double basePrice = base.evaluate(context);
                double bonusPrice = bonus.evaluate(context);
                float size = Optional.ofNullable(context.arg(ContextKeys.SIZE)).orElse(0f);
                double price = basePrice + bonusPrice * size;
                item.setTag(price, "Price");
                context.arg(ContextKeys.PRICE, price);
                context.arg(ContextKeys.PRICE_FORMATTED, String.format("%.2f", price));
            };
        }, 1_500, "price");
    }

    private void registerBuiltInEffectModifierParser() {
        this.registerEffectModifierParser(object -> {
            Section section = (Section) object;
            return builder -> builder.requirements(List.of(plugin.getRequirementManager().parseRequirements(section, true)));
        }, "requirements");
        this.registerEffectModifierParser(object -> {
            Section section = (Section) object;
            ArrayList<BiConsumer<Effect, Context<Player>>> consumers = new ArrayList<>();
            for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof Section innerSection) {
                    consumers.add(parseEffect(innerSection));
                }
            }
            return builder -> builder.modifiers(consumers);
        }, "effects");
    }

    private BiConsumer<Effect, Context<Player>> parseEffect(Section section) {
        switch (section.getString("type")) {
            case "weight-mod" -> {
                var op = parseWeightOperation(section.getStringList("value"));
                return (((effect, context) -> effect.weightOperations(op)));
            }
            case "weight-mod-ignore-conditions" -> {
                var op = parseWeightOperation(section.getStringList("value"));
                return (((effect, context) -> effect.weightOperationsIgnored(op)));
            }
            case "group-mod" -> {
                var op = parseGroupWeightOperation(section.getStringList("value"));
                return (((effect, context) -> effect.weightOperations(op)));
            }
            case "group-mod-ignore-conditions" -> {
                var op = parseGroupWeightOperation(section.getStringList("value"));
                return (((effect, context) -> effect.weightOperationsIgnored(op)));
            }
            case "wait-time" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.waitTimeAdder(effect.waitTimeAdder() + value.evaluate(context))));
            }
            case "hook-time", "wait-time-multiplier" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.waitTimeMultiplier(effect.waitTimeMultiplier() - 1 + value.evaluate(context))));
            }
            case "difficulty" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.difficultyAdder(effect.difficultyAdder() + value.evaluate(context))));
            }
            case "difficulty-multiplier", "difficulty-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.difficultyMultiplier(effect.difficultyMultiplier() - 1 + value.evaluate(context))));
            }
            case "size" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.sizeAdder(effect.sizeAdder() + value.evaluate(context))));
            }
            case "size-multiplier", "size-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.sizeMultiplier(effect.sizeMultiplier() - 1 + value.evaluate(context))));
            }
            case "game-time" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.gameTimeAdder(effect.gameTimeAdder() + value.evaluate(context))));
            }
            case "game-time-multiplier", "game-time-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.gameTimeMultiplier(effect.gameTimeMultiplier() - 1 + value.evaluate(context))));
            }
            case "score" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.scoreAdder(effect.scoreAdder() + value.evaluate(context))));
            }
            case "score-multiplier", "score-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.scoreMultiplier(effect.scoreMultiplier() - 1 + value.evaluate(context))));
            }
            case "multiple-loot" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context) -> effect.multipleLootChance(effect.multipleLootChance() + value.evaluate(context))));
            }
            case "lava-fishing" -> {
                return (((effect, context) -> effect.properties().put(EffectProperties.LAVA_FISHING, true)));
            }
            case "void-fishing" -> {
                return (((effect, context) -> effect.properties().put(EffectProperties.VOID_FISHING, true)));
            }
            case "conditional" -> {
                Requirement<Player>[] requirements = plugin.getRequirementManager().parseRequirements(section.getSection("conditions"), true);
                Section effectSection = section.getSection("effects");
                ArrayList<BiConsumer<Effect, Context<Player>>> effects = new ArrayList<>();
                if (effectSection != null)
                    for (Map.Entry<String, Object> entry : effectSection.getStringRouteMappedValues(false).entrySet())
                        if (entry.getValue() instanceof Section inner)
                            effects.add(parseEffect(inner));
                return (((effect, context) -> {
                    if (!RequirementManager.isSatisfied(context, requirements)) return;
                    for (BiConsumer<Effect, Context<Player>> consumer : effects) {
                        consumer.accept(effect, context);
                    }
                }));
            }
            default -> {
                return (((effect, context) -> {}));
            }
        }
    }

    private BiFunction<Context<Player>, Double, Double> parseWeightOperation(String op) {
        switch (op.charAt(0)) {
            case '/' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return (context, weight) -> weight / arg.evaluate(context);
            }
            case '*' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return (context, weight) -> weight * arg.evaluate(context);
            }
            case '-' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return (context, weight) -> weight - arg.evaluate(context);
            }
            case '%' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return (context, weight) -> weight % arg.evaluate(context);
            }
            case '+' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return (context, weight) -> weight + arg.evaluate(context);
            }
            case '=' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return (context, weight) -> {
                    context.arg(ContextKeys.WEIGHT, weight);
                    return arg.evaluate(context);
                };
            }
            default -> throw new IllegalArgumentException("Invalid weight operation: " + op);
        }
    }

    private List<Pair<String, BiFunction<Context<Player>, Double, Double>>> parseWeightOperation(List<String> ops) {
        List<Pair<String, BiFunction<Context<Player>, Double, Double>>> result = new ArrayList<>();
        for (String op : ops) {
            String[] split = op.split(":", 2);
            result.add(Pair.of(split[0], parseWeightOperation(split[1])));
        }
        return result;
    }

    private List<Pair<String, BiFunction<Context<Player>, Double, Double>>> parseGroupWeightOperation(List<String> gops) {
        List<Pair<String, BiFunction<Context<Player>, Double, Double>>> result = new ArrayList<>();
        for (String gop : gops) {
            String[] split = gop.split(":", 2);
            BiFunction<Context<Player>, Double, Double> operation = parseWeightOperation(split[1]);
            for (String member : plugin.getLootManager().getGroupMembers(split[0])) {
                result.add(Pair.of(member, operation));
            }
        }
        return result;
    }

    private void registerBuiltInBaseEffectParser() {
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.difficultyAdder(mathValue);
        }, "base-effects", "difficulty-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.difficultyMultiplier(mathValue);
        }, "base-effects", "difficulty-multiplier");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.gameTimeAdder(mathValue);
        }, "base-effects", "game-time-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.gameTimeMultiplier(mathValue);
        }, "base-effects", "game-time-multiplier");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.waitTimeAdder(mathValue);
        }, "base-effects", "wait-time-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.waitTimeMultiplier(mathValue);
        }, "base-effects", "wait-time-multiplier");
    }

    private void registerBuiltInEntityParser() {
        this.registerEntityParser(object -> {
            String entity = (String) object;
            return builder -> builder.entityID(entity);
        }, "entity");
        this.registerEntityParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.horizontalVector(mathValue);
        }, "velocity", "horizontal");
        this.registerEntityParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.verticalVector(mathValue);
        }, "velocity", "vertical");
        this.registerEntityParser(object -> {
            Section section = (Section) object;
            return builder -> builder.propertyMap(section.getStringRouteMappedValues(false));
        }, "properties");
    }

    private void registerBuiltInEventParser() {
        this.registerEventParser(object -> {
            boolean disable = (boolean) object;
            return builder -> builder.disableGlobalActions(disable);
        }, "disable-global-event");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.SUCCESS, actions);
        }, "events", "success");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.ACTIVATE, actions);
        }, "events", "activate");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.FAILURE, actions);
        }, "events", "failure");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.HOOK, actions);
        }, "events", "hook");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.CONSUME, actions);
        }, "events", "consume");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.CAST, actions);
        }, "events", "cast");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.BITE, actions);
        }, "events", "bite");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.LAND, actions);
        }, "events", "land");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.TIMER, actions);
        }, "events", "timer");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.INTERACT, actions);
        }, "events", "interact");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.NEW_SIZE_RECORD, actions);
        }, "events", "new_size_record");
    }

    private void registerBuiltInLootParser() {
        this.registerLootParser(object -> {
            String string = (String) object;
            return builder -> builder.nick(string);
        }, "nick");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.showInFinder(value);
        }, "show-in-fishfinder");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.disableStatistics(value);
        }, "disable-stat");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.disableGame(value);
        }, "disable-game");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.instantGame(value);
        }, "instant-game");
        this.registerLootParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.score(mathValue);
        }, "score");
        this.registerLootParser(object -> {
            List<String> args = ListUtils.toList(object);
            return builder -> builder.groups(args.toArray(new String[0]));
        }, "group");
        this.registerLootParser(object -> {
            Section section = (Section) object;
            StatisticsKeys keys = new StatisticsKeys(
                    section.getString("amount"),
                    section.getString("size")
            );
            return builder -> builder.statisticsKeys(keys);
        }, "statistics");
    }

    @Override
    public void saveResource(String filePath) {
        if (!new File(plugin.getDataFolder(), filePath).exists()) {
            plugin.getBoostrap().saveResource(filePath, false);
        }
    }
}
