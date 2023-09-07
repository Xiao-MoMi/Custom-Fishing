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

package net.momirealms.customfishing.mechanic.requirement;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Modifier;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class ConditionalLoots {

    private final List<Pair<String, Modifier>> modifierList;
    private final HashMap<String, ConditionalLoots> subLoots;
    private final Requirement[] requirements;

    public ConditionalLoots(
            Requirement[] requirements,
            List<Pair<String, Modifier>> modifierList,
            HashMap<String, ConditionalLoots> subLoots
    ) {
        this.modifierList = modifierList;
        this.requirements = requirements;
        this.subLoots = subLoots;
    }

    synchronized public void combine(Player player, HashMap<String, Double> weightMap) {
        for (Pair<String, Modifier> modifierPair : this.modifierList) {
            double previous = weightMap.getOrDefault(modifierPair.left(), 0d);
            weightMap.put(modifierPair.left(), modifierPair.right().modify(player, previous));
        }
    }

    public boolean isConditionsMet(Condition condition) {
        for (Requirement requirement : requirements) {
            if (!requirement.isConditionMet(condition)) {
                return false;
            }
        }
        return true;
    }

    public HashMap<String, ConditionalLoots> getSubLoots() {
        return subLoots;
    }
}
