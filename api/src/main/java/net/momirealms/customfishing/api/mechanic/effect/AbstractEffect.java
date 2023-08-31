package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Modifier;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class AbstractEffect implements Effect {

    protected boolean lavaFishing = false;
    protected double multipleLootChance = 0;
    protected double sizeMultiplier = 1;
    protected double scoreMultiplier = 1;
    protected double timeModifier = 1;
    protected double difficultyModifier = 0;
    protected double gameTimeModifier = 0;
    protected Requirement[] requirements;
    protected List<Pair<String, Modifier>> lootWeightModifier = new ArrayList<>();

    @Override
    public boolean persist() {
        return false;
    }

    @Override
    public Requirement[] getRequirements() {
        return requirements;
    }

    @Override
    public boolean canLavaFishing() {
        return lavaFishing;
    }

    @Override
    public double getMultipleLootChance() {
        return multipleLootChance;
    }

    @Override
    public double getSizeMultiplier() {
        return sizeMultiplier;
    }

    @Override
    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    @Override
    public double getTimeModifier() {
        return timeModifier;
    }

    @Override
    public double getGameTimeModifier() {
        return gameTimeModifier;
    }

    @Override
    public double getDifficultyModifier() {
        return difficultyModifier;
    }

    @Override
    public AbstractEffect merge(Effect another) {
        if (another == null) return this;
        if (another.canLavaFishing()) this.lavaFishing = true;
        this.scoreMultiplier += (another.getScoreMultiplier() -1);
        this.sizeMultiplier += (another.getSizeMultiplier() -1);
        this.timeModifier += (another.getTimeModifier() -1);
        this.multipleLootChance += another.getMultipleLootChance();
        this.difficultyModifier += another.getDifficultyModifier();
        this.gameTimeModifier += another.getGameTimeModifier();
        return this;
    }

    @Override
    public List<Pair<String, Modifier>> getLootWeightModifier() {
        return lootWeightModifier;
    }

    @Override
    public boolean canMerge(Condition condition) {
        if (this.requirements == null) return true;
        for (Requirement requirement : requirements) {
            if (!requirement.isConditionMet(condition)) {
                return false;
            }
        }
        return true;
    }
}
