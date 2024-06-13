package net.momirealms.customfishing.api.mechanic.config.function;

import net.momirealms.customfishing.api.mechanic.hook.HookConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public class HookParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<HookConfig.Builder>> function;

    public HookParserFunction(Function<Object, Consumer<HookConfig.Builder>> function) {
        this.function = function;
    }

    public Consumer<HookConfig.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.HOOK;
    }
}
