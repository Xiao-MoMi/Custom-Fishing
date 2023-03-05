package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.FishingCondition;
import net.momirealms.customfishing.integration.SkillInterface;

public record SkillLevelImpl(int level) implements RequirementInterface {

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
         SkillInterface skillInterface = CustomFishing.getInstance().getIntegrationManager().getSkillInterface();
         if (skillInterface == null) return false;
         return skillInterface.getLevel(fishingCondition.getPlayer()) >= level;
    }
}
