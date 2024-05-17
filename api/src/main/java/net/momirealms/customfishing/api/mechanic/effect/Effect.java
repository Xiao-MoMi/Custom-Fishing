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

import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiFunction;

public interface Effect {

    boolean allowLavaFishing();

    double multipleLootChance();

    double sizeAdder();

    double sizeMultiplier();

    double scoreAdder();

    double scoreMultiplier();

    double waitTimeAdder();

    double getWaitTimeMultiplier();

    double gameTimeAdder();

    double gameTimeMultiplier();

    double difficultyAdder();

    double difficultyMultiplier();

    List<Pair<String, BiFunction<Player, Double, Double>>> weightModifier();

    List<Pair<String, BiFunction<Player, Double, Double>>> weightModifierIgnored();

    void merge(Effect effect);

    interface Builder {

    }
}
