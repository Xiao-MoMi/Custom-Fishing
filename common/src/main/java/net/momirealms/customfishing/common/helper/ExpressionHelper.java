package net.momirealms.customfishing.common.helper;

import net.objecthunter.exp4j.ExpressionBuilder;

public class ExpressionHelper {

    public static double evaluate(String expression) {
        return new ExpressionBuilder(expression).build().evaluate();
    }
}
