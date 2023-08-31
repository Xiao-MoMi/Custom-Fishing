package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Modifier;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;

import java.util.List;

public interface Effect {

    boolean persist();

    Requirement[] getRequirements();

    boolean canLavaFishing();

    double getMultipleLootChance();

    double getSizeMultiplier();

    double getScoreMultiplier();

    double getTimeModifier();

    double getGameTimeModifier();

    double getDifficultyModifier();

    Effect merge(Effect another);

    List<Pair<String, Modifier>> getLootWeightModifier();

    boolean canMerge(Condition condition);
}
