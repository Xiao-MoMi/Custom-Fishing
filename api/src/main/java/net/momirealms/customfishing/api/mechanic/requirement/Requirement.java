package net.momirealms.customfishing.api.mechanic.requirement;

import net.momirealms.customfishing.api.mechanic.condition.Condition;

public interface Requirement {

    boolean isConditionMet(Condition condition);
}
