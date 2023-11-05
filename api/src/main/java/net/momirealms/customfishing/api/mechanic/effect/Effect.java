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

package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.loot.WeightModifier;

import java.util.List;

public interface Effect {

    boolean canLavaFishing();

    double getMultipleLootChance();

    double getSize();

    double getSizeMultiplier();

    double getScore();

    double getScoreMultiplier();

    double getWaitTime();

    double getWaitTimeMultiplier();

    double getGameTime();

    double getGameTimeMultiplier();

    double getDifficulty();

    double getDifficultyMultiplier();

    List<Pair<String, WeightModifier>> getWeightModifier();

    List<Pair<String, WeightModifier>> getWeightModifierIgnored();

    void merge(Effect effect);
}
