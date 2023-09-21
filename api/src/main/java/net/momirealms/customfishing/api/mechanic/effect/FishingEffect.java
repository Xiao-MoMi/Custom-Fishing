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

    /**
     * Sets whether lava fishing is enabled.
     *
     * @param lavaFishing True if lava fishing is enabled, false otherwise.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setLavaFishing(boolean lavaFishing) {
        this.lavaFishing = lavaFishing;
        return this;
    }

    /**
     * Sets the multiple loot chance.
     *
     * @param multipleLootChance The multiple loot chance value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setMultipleLootChance(double multipleLootChance) {
        this.multipleLootChance = multipleLootChance;
        return this;
    }

    /**
     * Sets the size multiplier.
     *
     * @param sizeMultiplier The size multiplier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setSizeMultiplier(double sizeMultiplier) {
        this.sizeMultiplier = sizeMultiplier;
        return this;
    }

    /**
     * Sets the score multiplier.
     *
     * @param scoreMultiplier The score multiplier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setScoreMultiplier(double scoreMultiplier) {
        this.scoreMultiplier = scoreMultiplier;
        return this;
    }

    /**
     * Sets the hook time modifier.
     *
     * @param timeModifier The hook time modifier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setHookTimeModifier(double timeModifier) {
        this.hookTimeModifier = timeModifier;
        return this;
    }

    /**
     * Sets the difficulty modifier.
     *
     * @param difficultyModifier The difficulty modifier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setDifficultyModifier(double difficultyModifier) {
        this.difficultyModifier = difficultyModifier;
        return this;
    }

    /**
     * Sets the game time modifier.
     *
     * @param gameTimeModifier The game time modifier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setGameTimeModifier(double gameTimeModifier) {
        this.gameTimeModifier = gameTimeModifier;
        return this;
    }

    /**
     * Adds weight modifiers to the FishingEffect.
     *
     * @param weightModifier A list of pairs representing weight modifiers to add.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect addWeightModifier(List<Pair<String, WeightModifier>> weightModifier) {
        this.weightModifier.addAll(weightModifier);
        return this;
    }

    /**
     * Adds ignored weight modifiers to the FishingEffect.
     *
     * @param weightModifierIgnored A list of pairs representing ignored weight modifiers to add.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect addWeightModifierIgnored(List<Pair<String, WeightModifier>> weightModifierIgnored) {
        this.weightModifierIgnored.addAll(weightModifierIgnored);
        return this;
    }

    /**
     * Checks if lava fishing is enabled.
     *
     * @return True if lava fishing is enabled, false otherwise.
     */
    @Override
    public boolean canLavaFishing() {
        return lavaFishing;
    }

    /**
     * Retrieves the multiple loot chance.
     *
     * @return The multiple loot chance value.
     */
    @Override
    public double getMultipleLootChance() {
        return multipleLootChance;
    }

    /**
     * Retrieves the size multiplier.
     *
     * @return The size multiplier value.
     */
    @Override
    public double getSizeMultiplier() {
        return sizeMultiplier;
    }

    /**
     * Retrieves the score multiplier.
     *
     * @return The score multiplier value.
     */
    @Override
    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    /**
     * Retrieves the hook time modifier.
     *
     * @return The hook time modifier value.
     */
    @Override
    public double getHookTimeModifier() {
        return hookTimeModifier;
    }

    /**
     * Retrieves the game time modifier.
     *
     * @return The game time modifier value.
     */
    @Override
    public double getGameTimeModifier() {
        return gameTimeModifier;
    }

    /**
     * Retrieves the difficulty modifier.
     *
     * @return The difficulty modifier value.
     */
    @Override
    public double getDifficultyModifier() {
        return difficultyModifier;
    }

    /**
     * Retrieves the list of weight modifiers.
     *
     * @return The list of weight modifiers.
     */
    @Override
    public List<Pair<String, WeightModifier>> getWeightModifier() {
        return weightModifier;
    }

    /**
     * Retrieves the list of weight modifiers ignoring conditions.
     *
     * @return The list of weight modifiers ignoring conditions.
     */
    @Override
    public List<Pair<String, WeightModifier>> getWeightModifierIgnored() {
        return weightModifierIgnored;
    }

    /**
     * Merges another Effect into this FishingEffect, combining their properties.
     *
     * @param another The Effect to merge into this FishingEffect.
     */
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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating instances of the {@link FishingEffect} class with customizable properties.
     */
    public static class Builder {

        private final FishingEffect effect;

        public Builder() {
            this.effect = new FishingEffect();
        }

        /**
         * Adds weight modifiers to the FishingEffect.
         *
         * @param modifier A list of pairs representing weight modifiers.
         * @return The Builder instance for method chaining.
         */
        public Builder addWeightModifier(List<Pair<String, WeightModifier>> modifier) {
            effect.weightModifier.addAll(modifier);
            return this;
        }

        /**
         * Adds weight modifiers ignoring conditions to the FishingEffect.
         *
         * @param modifier A list of pairs representing ignoring conditions weight modifiers.
         * @return The Builder instance for method chaining.
         */
        public Builder addWeightModifierIgnored(List<Pair<String, WeightModifier>> modifier) {
            effect.weightModifierIgnored.addAll(modifier);
            return this;
        }

        /**
         * Sets the multiple loot chance for the FishingEffect.
         *
         * @param multipleLootChance The multiple loot chance value to set.
         * @return The Builder instance for method chaining.
         */
        public Builder multipleLootChance(double multipleLootChance) {
            effect.multipleLootChance = multipleLootChance;
            return this;
        }

        /**
         * Sets the difficulty modifier for the FishingEffect.
         *
         * @param difficultyModifier The difficulty modifier value to set.
         * @return The Builder instance for method chaining.
         */
        public Builder difficultyModifier(double difficultyModifier) {
            effect.difficultyModifier = difficultyModifier;
            return this;
        }

        /**
         * Sets the size multiplier for the FishingEffect.
         *
         * @param sizeMultiplier The size multiplier value to set.
         * @return The Builder instance for method chaining.
         */
        public Builder sizeMultiplier(double sizeMultiplier) {
            effect.sizeMultiplier = sizeMultiplier;
            return this;
        }

        /**
         * Sets the time modifier for the FishingEffect.
         *
         * @param timeModifier The time modifier value to set.
         * @return The Builder instance for method chaining.
         */
        public Builder timeModifier(double timeModifier) {
            effect.hookTimeModifier = timeModifier;
            return this;
        }

        /**
         * Sets the score multiplier for the FishingEffect.
         *
         * @param scoreMultiplier The score multiplier value to set.
         * @return The Builder instance for method chaining.
         */
        public Builder scoreMultiplier(double scoreMultiplier) {
            effect.scoreMultiplier = scoreMultiplier;
            return this;
        }

        /**
         * Sets the game time modifier for the FishingEffect.
         *
         * @param gameTimeModifier The game time modifier value to set.
         * @return The Builder instance for method chaining.
         */
        public Builder gameTimeModifier(double gameTimeModifier) {
            effect.gameTimeModifier = gameTimeModifier;
            return this;
        }

        /**
         * Sets whether lava fishing is enabled for the FishingEffect.
         *
         * @param lavaFishing True if lava fishing is enabled, false otherwise.
         * @return The Builder instance for method chaining.
         */
        public Builder lavaFishing(boolean lavaFishing) {
            effect.lavaFishing = lavaFishing;
            return this;
        }

        /**
         * Builds and returns the finalized FishingEffect instance.
         *
         * @return The created FishingEffect instance.
         */
        public FishingEffect build() {
            return effect;
        }
    }
}
