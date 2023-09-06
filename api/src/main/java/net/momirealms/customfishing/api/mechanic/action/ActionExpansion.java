package net.momirealms.customfishing.api.mechanic.action;

public abstract class ActionExpansion {

    public abstract String getVersion();

    public abstract String getAuthor();

    public abstract String getActionType();

    public abstract ActionFactory getActionFactory();
}
