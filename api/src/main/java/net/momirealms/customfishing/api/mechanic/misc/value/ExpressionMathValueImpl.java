package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;

public class ExpressionMathValueImpl<T> implements MathValue<T> {

    private String raw;

    public ExpressionMathValueImpl(String raw) {
        this.raw = raw;
    }

    @Override
    public double evaluate(Context<T> context) {
        return 0;
    }
}
