package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.FishingCondition;

public record SkillLevelImpl(int level) implements RequirementInterface {

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        return CustomFishing.getInstance().getIntegrationManager().getSkillInterface().getLevel(fishingCondition.getPlayer()) >= level;
    }
}
