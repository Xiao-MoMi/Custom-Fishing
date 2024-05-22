package net.momirealms.customfishing.api.mechanic.config.function;

import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;

import java.util.function.Consumer;
import java.util.function.Function;

public class EffectModifierParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<EffectModifier.Builder>> function;

    public EffectModifierParserFunction(Function<Object, Consumer<EffectModifier.Builder>> function) {
        this.function = function;
    }

    public Consumer<EffectModifier.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.EFFECT_MODIFIER;
    }
}
