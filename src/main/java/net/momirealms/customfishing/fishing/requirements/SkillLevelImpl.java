package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.FishingCondition;
import net.momirealms.customfishing.integration.SkillInterface;
import org.jetbrains.annotations.Nullable;

public class SkillLevelImpl extends Requirement implements RequirementInterface {

    private final int level;

    public SkillLevelImpl(@Nullable String[] msg, int level) {
        super(msg);
        this.level = level;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
         SkillInterface skillInterface = CustomFishing.getInstance().getIntegrationManager().getSkillInterface();
         if (skillInterface == null) return true;
         if (skillInterface.getLevel(fishingCondition.getPlayer()) >= level) {
             return true;
         }
         notMetMessage(fishingCondition.getPlayer());
         return false;
    }
}
