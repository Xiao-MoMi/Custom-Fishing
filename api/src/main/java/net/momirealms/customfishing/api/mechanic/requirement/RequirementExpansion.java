package net.momirealms.customfishing.api.mechanic.requirement;

public abstract class RequirementExpansion {

    public abstract String getVersion();

    public abstract String getAuthor();

    public abstract String getRequirementType();

    public abstract RequirementFactory getRequirementFactory();
}
