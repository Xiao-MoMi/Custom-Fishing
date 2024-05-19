package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.BukkitPlaceholderManager;
import net.momirealms.customfishing.common.helper.ExpressionHelper;
import org.bukkit.OfflinePlayer;

import java.util.Map;

public class ExpressionMathValueImpl<T> implements MathValue<T> {

    private final String raw;

    public ExpressionMathValueImpl(String raw) {
        this.raw = raw;
    }

    @Override
    public double evaluate(Context<T> context) {
        Map<String, String> replacements = context.toPlaceholderMap();
        String expression;
        if (context.getHolder() instanceof OfflinePlayer player) expression = BukkitPlaceholderManager.getInstance().parse(player, raw, replacements);
        else expression = BukkitPlaceholderManager.getInstance().parse(null, raw, replacements);
        return ExpressionHelper.evaluate(expression);
    }
}
