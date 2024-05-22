package net.momirealms.customfishing.api.mechanic.config.function;

import net.momirealms.customfishing.api.mechanic.block.BlockConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public class BlockParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<BlockConfig.Builder>> function;

    public BlockParserFunction(Function<Object, Consumer<BlockConfig.Builder>> function) {
        this.function = function;
    }

    public Consumer<BlockConfig.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.BLOCK;
    }
}
