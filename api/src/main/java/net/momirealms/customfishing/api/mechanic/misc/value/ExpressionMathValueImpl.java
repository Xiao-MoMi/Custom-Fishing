/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        Map<String, String> replacements = context.placeholderMap();
        String expression;
        if (context.getHolder() instanceof OfflinePlayer player) expression = BukkitPlaceholderManager.getInstance().parse(player, raw, replacements);
        else expression = BukkitPlaceholderManager.getInstance().parse(null, raw, replacements);
        return ExpressionHelper.evaluate(expression);
    }
}
