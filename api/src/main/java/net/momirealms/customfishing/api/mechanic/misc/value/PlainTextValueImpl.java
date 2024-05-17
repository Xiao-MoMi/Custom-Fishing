package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;

public class PlainTextValueImpl<T> implements TextValue<T> {

    private final String raw;

    public PlainTextValueImpl(String raw) {
        this.raw = raw;
    }

    @Override
    public String render(Context<T> context) {
        return raw;
    }
}
