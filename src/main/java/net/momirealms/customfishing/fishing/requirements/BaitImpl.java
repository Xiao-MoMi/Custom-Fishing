package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.fishing.FishingCondition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class BaitImpl extends Requirement implements RequirementInterface {

    private final HashSet<String> baits;

    public BaitImpl(@Nullable String[] msg, HashSet<String> baits) {
        super(msg);
        this.baits = baits;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        Player player = fishingCondition.getPlayer();
        String bait = fishingCondition.getBaitID();
        if (bait == null || baits.contains(bait)) {
            return true;
        }
        notMetMessage(player);
        return false;
    }
}
