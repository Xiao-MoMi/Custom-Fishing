package net.momirealms.customfishing.api.mechanic.config.function;

import net.momirealms.customfishing.api.mechanic.totem.TotemConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public class TotemParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<TotemConfig.Builder>> function;

    public TotemParserFunction(Function<Object, Consumer<TotemConfig.Builder>> function) {
        this.function = function;
    }

    public Consumer<TotemConfig.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.TOTEM;
    }
}
