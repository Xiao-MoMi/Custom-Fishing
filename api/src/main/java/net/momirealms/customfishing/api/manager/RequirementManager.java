package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface RequirementManager {

    boolean registerRequirement(String type, RequirementBuilder requirementBuilder);

    boolean unregisterRequirement(String type);

    HashMap<String, Double> getLootWithWeight(Condition condition);

    @Nullable Requirement[] getRequirements(ConfigurationSection section, boolean advanced);

    Requirement getRequirement(ConfigurationSection section, boolean checkAction);

    Requirement getRequirement(String key, Object value);

    RequirementBuilder getRequirementBuilder(String type);

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
