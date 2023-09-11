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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface RequirementManager {

    boolean registerRequirement(String type, RequirementFactory requirementFactory);

    boolean unregisterRequirement(String type);

    HashMap<String, Double> getLootWithWeight(Condition condition);

    HashMap<String, Double> getGameWithWeight(Condition condition);

    @Nullable Requirement[] getRequirements(ConfigurationSection section, boolean advanced);

    Requirement getRequirement(ConfigurationSection section, boolean checkAction);

    Requirement getRequirement(String key, Object value);

    RequirementFactory getRequirementBuilder(String type);

    static boolean isRequirementsMet(Requirement[] requirements, Condition condition) {
        if (requirements == null) return true;
        for (Requirement requirement : requirements) {
            if (!requirement.isConditionMet(condition)) {
                return false;
            }
        }
        return true;
    }

    static boolean isRequirementMet(Requirement requirement, Condition condition) {
        if (requirement == null) return true;
        return requirement.isConditionMet(condition);
    }
}
