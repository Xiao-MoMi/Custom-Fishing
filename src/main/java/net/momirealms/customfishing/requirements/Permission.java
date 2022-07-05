package net.momirealms.customfishing.requirements;

public record Permission(String permission) implements Requirement {

    public String getPermission() {
        return this.permission;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        return fishingCondition.getPlayer().hasPermission(permission);
    }
}