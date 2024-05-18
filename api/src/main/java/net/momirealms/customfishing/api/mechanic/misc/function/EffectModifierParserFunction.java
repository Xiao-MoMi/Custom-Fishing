package net.momirealms.customfishing.api.mechanic.misc.function;

import net.momirealms.customfishing.api.mechanic.effect.Effect;

import java.util.function.Consumer;
import java.util.function.Function;

public class EffectModifierParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<Effect>> function;

    public EffectModifierParserFunction(Function<Object, Consumer<Effect>> function) {
        this.function = function;
    }

    public Consumer<Effect> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.EFFECT_MODIFIER;
    }
}
