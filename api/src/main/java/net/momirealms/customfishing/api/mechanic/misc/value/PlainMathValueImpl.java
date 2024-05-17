package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;

public class PlainMathValueImpl<T> implements MathValue<T> {

    private final double value;

    public PlainMathValueImpl(double value) {
        this.value = value;
    }

    @Override
    public double evaluate(Context<T> context) {
        return value;
    }
}
