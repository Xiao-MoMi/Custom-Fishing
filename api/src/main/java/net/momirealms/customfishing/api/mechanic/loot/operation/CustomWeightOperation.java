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

package net.momirealms.customfishing.api.mechanic.loot.operation;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomWeightOperation implements WeightOperation {

    private final MathValue<Player> arg;
    private final boolean hasTotalWeight;
    private final List<String> otherWeights;

    public CustomWeightOperation(MathValue<Player> arg, boolean hasTotalWeight, List<String> otherWeights) {
        this.arg = arg;
        this.hasTotalWeight = hasTotalWeight;
        this.otherWeights = otherWeights;
    }

    @Override
    public Double apply(Context<Player> context, Double weight, Map<String, Double> weights) {
        context.arg(ContextKeys.WEIGHT, weight);
        if (hasTotalWeight) {
            context.arg(ContextKeys.TOTAL_WEIGHT, getValidTotalWeight(weights.values()));
        }
        if (!otherWeights.isEmpty()) {
            for (String otherWeight : otherWeights) {
                context.arg(ContextKeys.of("loot_" + otherWeight, Double.class), weights.get(otherWeight));
            }
        }
        return arg.evaluate(context);
    }

    private double getValidTotalWeight(Collection<Double> weights) {
        double totalWeight = 0;
        for (Double weight : weights) {
            if (weight > 0) {
                totalWeight += weight;
            }
        }
        return totalWeight;
    }
}
