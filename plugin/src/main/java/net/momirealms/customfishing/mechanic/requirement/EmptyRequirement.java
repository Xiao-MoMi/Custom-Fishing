package net.momirealms.customfishing.mechanic.requirement;

import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;

public class EmptyRequirement implements Requirement {

    public static EmptyRequirement instance = new EmptyRequirement();

    @Override
    public boolean isConditionMet(Condition condition) {
        return true;
    }
}
