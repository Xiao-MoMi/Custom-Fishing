package net.momirealms.customfishing.api.mechanic.requirement;

import net.momirealms.customfishing.api.mechanic.action.Action;

import java.util.List;

public interface RequirementBuilder {

    Requirement build(Object args, List<Action> notMetActions, boolean checkAction);

    default Requirement build(Object args) {
        return build(args, null, false);
    }
}
