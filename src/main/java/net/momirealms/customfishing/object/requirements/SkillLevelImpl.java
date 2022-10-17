package net.momirealms.customfishing.object.requirements;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.fishing.FishingCondition;

public record SkillLevelImpl(int level) implements RequirementInterface {

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {

        return CustomFishing.plugin.getIntegrationManager().getSkillInterface().getLevel(fishingCondition.getPlayer()) >= level;
    }
}
