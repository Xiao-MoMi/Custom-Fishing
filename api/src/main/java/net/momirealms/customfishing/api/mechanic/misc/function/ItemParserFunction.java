package net.momirealms.customfishing.api.mechanic.misc.function;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ItemParserFunction implements ConfigParserFunction {

    private final Function<Object, BiConsumer<Item<ItemStack>, Context<Player>>> function;
    private final int priority;

    public ItemParserFunction(int priority, Function<Object, BiConsumer<Item<ItemStack>, Context<Player>>> function) {
        this.function = function;
        this.priority = priority;
    }

    public BiConsumer<Item<ItemStack>, Context<Player>> accept(Object object) {
        return function.apply(object);
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public ParserType type() {
        return ParserType.ITEM;
    }
}
