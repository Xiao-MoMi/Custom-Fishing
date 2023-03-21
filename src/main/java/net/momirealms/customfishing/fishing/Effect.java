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

package net.momirealms.customfishing.fishing;

import net.momirealms.customfishing.fishing.requirements.RequirementInterface;
import net.momirealms.customfishing.manager.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Effect {

    private HashMap<String, Double> weightMD;
    private HashMap<String, Integer> weightAS;
    private double timeModifier;
    private double sizeMultiplier;
    private double scoreMultiplier;
    private int difficulty;
    private double doubleLootChance;
    private boolean canLavaFishing;
    private boolean hasSpecialRod;
    private RequirementInterface[] requirements;

    public void setSizeMultiplier(double sizeMultiplier) {
        this.sizeMultiplier = sizeMultiplier;
    }

    public HashMap<String, Double> getWeightMD() {
        return weightMD;
    }

    public void setWeightMD(HashMap<String, Double> weightMD) {
        this.weightMD = weightMD;
    }

    public HashMap<String, Integer> getWeightAS() {
        return weightAS;
    }

    public void setWeightAS(HashMap<String, Integer> weightAS) {
        this.weightAS = weightAS;
    }

    public double getTimeModifier() {
        return timeModifier;
    }

    public void setTimeModifier(double timeModifier) {
        this.timeModifier = timeModifier;
    }

    public void setScoreMultiplier(double scoreMultiplier) {
        this.scoreMultiplier = scoreMultiplier;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getDoubleLootChance() {
        return doubleLootChance;
    }

    public void setDoubleLootChance(double doubleLootChance) {
        this.doubleLootChance = doubleLootChance;
    }

    public boolean canLavaFishing() {
        return canLavaFishing || ConfigManager.allRodsFishInLava;
    }

    public void setCanLavaFishing(boolean canLavaFishing) {
        this.canLavaFishing = canLavaFishing;
    }

    public RequirementInterface[] getRequirements() {
        return requirements;
    }

    public void setRequirements(RequirementInterface[] requirements) {
        this.requirements = requirements;
    }

    public boolean addEffect(Effect anotherEffect, FishingCondition fishingCondition) {
        if (anotherEffect.getRequirements() != null) {
            for (RequirementInterface requirement : anotherEffect.getRequirements()) {
                if (!requirement.isConditionMet(fishingCondition)) {
                    return false;
                }
            }
        }
        HashMap<String, Integer> weightAS = anotherEffect.getWeightAS();
        if (weightAS != null) {
            for (Map.Entry<String, Integer> en : weightAS.entrySet()) {
                String group = en.getKey();
                this.weightAS.put(group, Optional.ofNullable(this.weightAS.get(group)).orElse(0) + en.getValue());
            }
        }
        HashMap<String, Double> weightMD = anotherEffect.getWeightMD();
        if (weightMD != null){
            for (Map.Entry<String, Double> en : weightMD.entrySet()) {
                String group = en.getKey();
                this.weightMD.put(group, Optional.ofNullable(this.weightMD.get(group)).orElse(1d) + en.getValue());
            }
        }
        if (anotherEffect.getTimeModifier() != 0) this.timeModifier += (anotherEffect.getTimeModifier() - 1);
        if (anotherEffect.getDoubleLootChance() != 0) this.doubleLootChance += anotherEffect.getDoubleLootChance();
        if (anotherEffect.getDifficulty() != 0) this.difficulty += anotherEffect.getDifficulty();
        if (anotherEffect.getScoreMultiplier() != 0) this.scoreMultiplier += (anotherEffect.getScoreMultiplier() - 1);
        if (anotherEffect.getSizeMultiplier() != 0) this.sizeMultiplier += (anotherEffect.getSizeMultiplier() - 1);
        if (anotherEffect.canLavaFishing()) this.canLavaFishing = true;
        return true;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public double getSizeMultiplier() {
        return sizeMultiplier;
    }

    public boolean hasSpecialRod() {
        return hasSpecialRod;
    }

    public void setHasSpecialRod(boolean hasSpecialRod) {
        this.hasSpecialRod = hasSpecialRod;
    }
}