package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.util.RandomUtils;

public class RangedMathValueImpl<T> implements MathValue<T> {

    private final double min;
    private final double max;

    public RangedMathValueImpl(String value) {
        String[] split = value.split("~");
        if (split.length != 2) {
            throw new IllegalArgumentException("Correct ranged format `a~b`");
        }
        double min = Double.parseDouble(split[0]);
        double max = Double.parseDouble(split[1]);
        if (min > max) {
            double temp = max;
            max = min;
            min = temp;
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public double evaluate(Context<T> context) {
        return RandomUtils.generateRandomDouble(min, max);
    }
}
