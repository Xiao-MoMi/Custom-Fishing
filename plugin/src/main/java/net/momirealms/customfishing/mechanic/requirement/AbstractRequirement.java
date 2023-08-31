package net.momirealms.customfishing.mechanic.requirement;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;

import java.util.List;

public abstract class AbstractRequirement implements Requirement {

    private final List<Action> actions;

    public AbstractRequirement(List<Action> actions) {
        this.actions = actions;
    }

    protected void triggerActions(Condition condition) {
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }
}
