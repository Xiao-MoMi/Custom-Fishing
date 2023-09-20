package net.momirealms.customfishing.mechanic.action;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.Condition;

/**
 * An implementation of the Action interface that represents an empty action with no behavior.
 * This class serves as a default action to prevent NPE.
 */
public class EmptyAction implements Action {

    public static EmptyAction instance = new EmptyAction();

    @Override
    public void trigger(Condition condition) {
    }
}
