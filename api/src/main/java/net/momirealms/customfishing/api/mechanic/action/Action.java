package net.momirealms.customfishing.api.mechanic.action;

import net.momirealms.customfishing.api.mechanic.condition.Condition;

public interface Action {

    void trigger(Condition condition);

}
