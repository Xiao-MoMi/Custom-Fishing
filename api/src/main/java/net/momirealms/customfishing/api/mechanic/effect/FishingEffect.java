/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.loot.WeightModifier;

import java.util.ArrayList;
import java.util.List;

public class FishingEffect implements Effect {

    private boolean lavaFishing = false;
    private double multipleLootChance = 0;
    private double sizeMultiplier = 1;
    private double scoreMultiplier = 1;
    private double hookTimeModifier = 1;
    private double difficultyModifier = 0;
    private double gameTimeModifier = 0;
    private final List<Pair<String, WeightModifier>> weightModifier = new ArrayList<>();
    private final List<Pair<String, WeightModifier>> weightModifierIgnored = new ArrayList<>();

    public FishingEffect setLavaFishing(boolean lavaFishing) {
        this.lavaFishing = lavaFishing;
        return this;
    }

    public FishingEffect setMultipleLootChance(double multipleLootChance) {
        this.multipleLootChance = multipleLootChance;
        return this;
    }

    public FishingEffect setSizeMultiplier(double sizeMultiplier) {
        this.sizeMultiplier = sizeMultiplier;
        return this;
    }

    public FishingEffect setScoreMultiplier(double scoreMultiplier) {
        this.scoreMultiplier = scoreMultiplier;
        return this;
    }

    public FishingEffect setHookTimeModifier(double timeModifier) {
        this.hookTimeModifier = timeModifier;
        return this;
    }

    public FishingEffect setDifficultyModifier(double difficultyModifier) {
        this.difficultyModifier = difficultyModifier;
        return this;
    }

    public FishingEffect setGameTimeModifier(double gameTimeModifier) {
        this.gameTimeModifier = gameTimeModifier;
        return this;
    }

    public FishingEffect addWeightModifier(List<Pair<String, WeightModifier>> weightModifier) {
        this.weightModifier.addAll(weightModifier);
        return this;
    }

    public FishingEffect addWeightModifierIgnored(List<Pair<String, WeightModifier>> weightModifierIgnored) {
        this.weightModifierIgnored.addAll(weightModifierIgnored);
        return this;
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
    public double getHookTimeModifier() {
        return hookTimeModifier;
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
    public List<Pair<String, WeightModifier>> getWeightModifier() {
        return weightModifier;
    }

    @Override
    public List<Pair<String, WeightModifier>> getWeightModifierIgnored() {
        return weightModifierIgnored;
    }

    @Override
    public void merge(Effect another) {
        if (another == null) return;
        if (another.canLavaFishing()) this.lavaFishing = true;
        this.scoreMultiplier += (another.getScoreMultiplier() -1);
        this.sizeMultiplier += (another.getSizeMultiplier() -1);
        this.hookTimeModifier += (another.getHookTimeModifier() -1);
        this.multipleLootChance += another.getMultipleLootChance();
        this.difficultyModifier += another.getDifficultyModifier();
        this.gameTimeModifier += another.getGameTimeModifier();
        this.weightModifierIgnored.addAll(another.getWeightModifierIgnored());
        this.weightModifier.addAll(another.getWeightModifier());
    }

    public static class Builder {

        private final FishingEffect effect;

        public Builder() {
            this.effect = new FishingEffect();
        }

        public Builder addWeightModifier(List<Pair<String, WeightModifier>> modifier) {
            effect.weightModifier.addAll(modifier);
            return this;
        }

        public Builder addWeightModifierIgnored(List<Pair<String, WeightModifier>> modifier) {
            effect.weightModifierIgnored.addAll(modifier);
            return this;
        }

        public Builder multipleLootChance(double multipleLootChance) {
            effect.multipleLootChance = multipleLootChance;
            return this;
        }

        public Builder difficultyModifier(double difficultyModifier) {
            effect.difficultyModifier = difficultyModifier;
            return this;
        }

        public Builder sizeMultiplier(double sizeMultiplier) {
            effect.sizeMultiplier = sizeMultiplier;
            return this;
        }

        public Builder timeModifier(double timeModifier) {
            effect.hookTimeModifier = timeModifier;
            return this;
        }

        public Builder scoreMultiplier(double scoreMultiplier) {
            effect.scoreMultiplier = scoreMultiplier;
            return this;
        }

        public Builder gameTimeModifier(double gameTimeModifier) {
            effect.gameTimeModifier = gameTimeModifier;
            return this;
        }

        public Builder lavaFishing(boolean lavaFishing) {
            effect.lavaFishing = lavaFishing;
            return this;
        }

        public FishingEffect build() {
            return effect;
        }
    }
}
