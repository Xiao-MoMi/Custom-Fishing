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
    private double waitTime = 0;
    private double waitTimeMultiplier = 1;
    private double size = 0;
    private double sizeMultiplier = 1;
    private double score = 0;
    private double scoreMultiplier = 1;
    private double difficulty = 0;
    private double difficultyMultiplier = 1;
    private double gameTime = 0;
    private double gameTimeMultiplier = 1;

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
     * Sets the size.
     *
     * @param size The size value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setSize(double size) {
        this.size = size;
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
     * Sets the score
     *
     * @param score The score value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setScore(double score) {
        this.score = score;
        return this;
    }

    /**
     * Sets the wait time multiplier.
     *
     * @param timeMultiplier The wait time multiplier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setWaitTimeMultiplier(double timeMultiplier) {
        this.waitTimeMultiplier = timeMultiplier;
        return this;
    }

    /**
     * Sets the wait time.
     *
     * @param waitTime The wait time value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setWaitTime(double waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    /**
     * Sets the difficulty.
     *
     * @param difficulty The difficulty value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setDifficulty(double difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    /**
     * Sets the difficulty multiplier.
     *
     * @param difficultyMultiplier The difficulty multiplier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setDifficultyMultiplier(double difficultyMultiplier) {
        this.difficultyMultiplier = difficultyMultiplier;
        return this;
    }

    /**
     * Sets the game time.
     *
     * @param gameTime The game time value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setGameTime(double gameTime) {
        this.gameTime = gameTime;
        return this;
    }

    /**
     * Sets the game time multiplier.
     *
     * @param gameTimeMultiplier The game time multiplier value to set.
     * @return The FishingEffect instance for method chaining.
     */
    public FishingEffect setGameTimeMultiplier(double gameTimeMultiplier) {
        this.gameTimeMultiplier = gameTimeMultiplier;
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
     * Retrieves the size.
     *
     * @return The size value.
     */
    @Override
    public double getSize() {
        return size;
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
     * Retrieves the wait time multiplier.
     *
     * @return The wait time multiplier value.
     */
    @Override
    public double getWaitTimeMultiplier() {
        return waitTimeMultiplier;
    }

    /**
     * Retrieves the wait time.
     *
     * @return The wait time .
     */
    @Override
    public double getWaitTime() {
        return waitTime;
    }

    /**
     * Retrieves the game time.
     *
     * @return The game time value.
     */
    @Override
    public double getGameTime() {
        return gameTime;
    }

    /**
     * Retrieves the game time multiplier.
     *
     * @return The game time value multiplier.
     */
    @Override
    public double getGameTimeMultiplier() {
        return gameTimeMultiplier;
    }

    /**
     * Retrieves score modifier.
     *
     * @return The score value.
     */
    @Override
    public double getScore() {
        return score;
    }

    /**
     * Retrieves the difficulty.
     *
     * @return The difficulty value.
     */
    @Override
    public double getDifficulty() {
        return difficulty;
    }

    /**
     * Retrieves the difficulty multiplier.
     *
     * @return The difficulty multiplier value.
     */
    @Override
    public double getDifficultyMultiplier() {
        return difficultyMultiplier;
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
        this.score += another.getScore();
        this.sizeMultiplier += (another.getSizeMultiplier() -1);
        this.size += another.getSize();
        this.difficultyMultiplier += (another.getDifficultyMultiplier() -1);
        this.difficulty += another.getDifficulty();
        this.gameTimeMultiplier += (another.getGameTimeMultiplier() - 1);
        this.gameTime += another.getGameTime();
        this.waitTimeMultiplier += (another.getWaitTimeMultiplier() -1);
        this.multipleLootChance += another.getMultipleLootChance();
        this.weightModifierIgnored.addAll(another.getWeightModifierIgnored());
        this.weightModifier.addAll(another.getWeightModifier());
    }
}
