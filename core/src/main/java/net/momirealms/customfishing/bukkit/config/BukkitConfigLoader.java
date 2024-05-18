package net.momirealms.customfishing.bukkit.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsKeys;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.util.ListUtils;
import net.momirealms.customfishing.common.util.RandomUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BukkitConfigLoader extends ConfigManager {

    public BukkitConfigLoader(CustomFishingPlugin plugin) {
        super(plugin);
        this.registerBuiltInItemProperties();
        this.registerBuiltInBaseEffectParser();
        this.registerBuiltInLootParser();
    }

    private void registerBuiltInItemProperties() {
        this.registerItemParser(arg -> {
            MathValue<Player> mathValue = MathValue.auto(arg);
            return (item, context) -> item.customModelData((int) mathValue.evaluate(context));
        }, 5000, "custom-model-data");
        this.registerItemParser(arg -> {
            TextValue<Player> textValue = TextValue.auto((String) arg);
            return (item, context) -> {
                item.displayName(AdventureHelper.miniMessageToJson(textValue.render(context)));
            };
        }, 4000, "display", "name");
        this.registerItemParser(arg -> {
            List<String> list = ListUtils.toList(arg);
            List<TextValue<Player>> lore = new ArrayList<>();
            for (String text : list) {
                lore.add(TextValue.auto(text));
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
                item.setTag(context.arg(ContextKeys.TYPE), "CustomFishing", "type");
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
            };
        }, 1_000, "size");
        this.registerItemParser(arg -> {
            Section section = (Section) arg;
            MathValue<Player> base = MathValue.auto(section.get("base"));
            MathValue<Player> bonus = MathValue.auto(section.get("bonus"));
            return (item, context) -> {
                double basePrice = base.evaluate(context);
                double bonusPrice = bonus.evaluate(context);
                float size = Optional.ofNullable(context.arg(ContextKeys.SIZE)).orElse(0f);
                double price = basePrice + bonusPrice * size;
                item.setTag(price, "CustomFishing", "price");
                context.arg(ContextKeys.PRICE, price);
            };
        }, 1_500, "price");
    }

    private void registerBuiltInBaseEffectParser() {
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.difficultyAdder(mathValue);
        }, "base-difficulty-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.difficultyMultiplier(mathValue);
        }, "base-difficulty-multiplier");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.gameTimeAdder(mathValue);
        }, "base-game-time-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.gameTimeMultiplier(mathValue);
        }, "base-game-time-multiplier");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.waitTimeAdder(mathValue);
        }, "base-wait-time-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.waitTimeMultiplier(mathValue);
        }, "base-wait-time-multiplier");
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
}
