package net.momirealms.customfishing.api.mechanic.config.function;

import net.momirealms.customfishing.api.mechanic.event.EventCarrier;

import java.util.function.Consumer;
import java.util.function.Function;

public class EventParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<EventCarrier.Builder>> function;

    public EventParserFunction(Function<Object, Consumer<EventCarrier.Builder>> function) {
        this.function = function;
    }

    public Consumer<EventCarrier.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.EVENT;
    }
}
