package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;

public class PlaceholderTextValueImpl<T> implements TextValue<T> {

    private final String raw;

    public PlaceholderTextValueImpl(String raw) {
        this.raw = raw;
    }

    @Override
    public String render(Context<T> context) {
        return raw;
    }
}
