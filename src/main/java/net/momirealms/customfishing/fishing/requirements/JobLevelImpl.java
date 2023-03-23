package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.FishingCondition;
import net.momirealms.customfishing.integration.JobInterface;
import org.jetbrains.annotations.Nullable;

public class JobLevelImpl extends Requirement implements RequirementInterface {

    private final int level;

    public JobLevelImpl(@Nullable String[] msg, int level) {
        super(msg);
        this.level = level;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        JobInterface jobInterface = CustomFishing.getInstance().getIntegrationManager().getJobInterface();
        if (jobInterface == null || fishingCondition.getPlayer() == null) return true;
        if (jobInterface.getLevel(fishingCondition.getPlayer()) >= level) {
            return true;
        }
        notMetMessage(fishingCondition.getPlayer());
        return false;
    }
}
