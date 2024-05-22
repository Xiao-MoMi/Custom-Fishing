package net.momirealms.customfishing.api.mechanic.config.function;

import net.momirealms.customfishing.api.mechanic.entity.EntityConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public class EntityParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<EntityConfig.Builder>> function;

    public EntityParserFunction(Function<Object, Consumer<EntityConfig.Builder>> function) {
        this.function = function;
    }

    public Consumer<EntityConfig.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.ENTITY;
    }
}
