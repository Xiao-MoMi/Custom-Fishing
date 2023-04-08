package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.fishing.FishingCondition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class RodImpl extends Requirement implements RequirementInterface {

    private final HashSet<String> rods;

    public RodImpl(@Nullable String[] msg, HashSet<String> rods) {
        super(msg);
        this.rods = rods;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        Player player = fishingCondition.getPlayer();
        String rod = fishingCondition.getRod_id();
        if (rod == null || rods.contains(rod)) {
            return true;
        }
        notMetMessage(player);
        return false;
    }
}
