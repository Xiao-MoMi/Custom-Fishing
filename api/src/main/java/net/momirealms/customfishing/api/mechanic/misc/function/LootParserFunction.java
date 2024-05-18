package net.momirealms.customfishing.api.mechanic.misc.function;

import net.momirealms.customfishing.api.mechanic.loot.Loot;

import java.util.function.Consumer;
import java.util.function.Function;

public class LootParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<Loot.Builder>> function;

    public LootParserFunction(Function<Object, Consumer<Loot.Builder>> function) {
        this.function = function;
    }

    public Consumer<Loot.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.LOOT;
    }
}
