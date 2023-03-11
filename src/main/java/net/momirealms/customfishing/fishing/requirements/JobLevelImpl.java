package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.FishingCondition;
import net.momirealms.customfishing.integration.JobInterface;

public record JobLevelImpl(int level) implements RequirementInterface {

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        JobInterface jobInterface = CustomFishing.getInstance().getIntegrationManager().getJobInterface();
        if (jobInterface == null) return true;
        return jobInterface.getLevel(fishingCondition.getPlayer()) >= level;
    }
}
