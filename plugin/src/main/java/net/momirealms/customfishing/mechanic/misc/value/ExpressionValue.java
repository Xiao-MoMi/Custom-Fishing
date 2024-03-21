/*
 *  Copyright (C) <2022> <XiaoMoMi>
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

package net.momirealms.customfishing.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.misc.Value;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.entity.Player;

import java.util.Map;

public class ExpressionValue implements Value {

    private final String expression;

    public ExpressionValue(String expression) {
        this.expression = expression;
    }

    @Override
    public double get(Player player, Map<String, String> values) {
        return ConfigUtils.getExpressionValue(player, expression, values);
    }
}
