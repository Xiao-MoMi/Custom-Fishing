package net.momirealms.customfishing.requirements;

import net.momirealms.customfishing.ConfigReader;

public record SkillLevel(int level) implements Requirement{
    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        return level <= ConfigReader.Config.skillXP.getLevel(fishingCondition.getPlayer());
    }
}
