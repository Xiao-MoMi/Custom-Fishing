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

package net.momirealms.customfishing.object.fishing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Bonus {

    private HashMap<String, Double> weightMD;
    private HashMap<String, Integer> weightAS;
    private double time;
    private double score;
    private int difficulty;
    private double doubleLoot;
    private boolean canLavaFishing;

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

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getDoubleLoot() {
        return doubleLoot;
    }

    public void setDoubleLoot(double doubleLoot) {
        this.doubleLoot = doubleLoot;
    }

    public boolean canLavaFishing() {
        return canLavaFishing;
    }

    public void setCanLavaFishing(boolean canLavaFishing) {
        this.canLavaFishing = canLavaFishing;
    }

    public void addBonus(Bonus anotherBonus) {
        HashMap<String, Integer> weightAS = anotherBonus.getWeightAS();
        if (weightAS != null) {
            for (Map.Entry<String, Integer> en : weightAS.entrySet()) {
                String group = en.getKey();
                this.weightAS.put(group, Optional.ofNullable(this.weightAS.get(group)).orElse(0) + en.getValue());
            }
        }
        HashMap<String, Double> weightMD = anotherBonus.getWeightMD();
        if (weightMD != null){
            for (Map.Entry<String, Double> en : weightMD.entrySet()) {
                String group = en.getKey();
                this.weightMD.put(group, Optional.ofNullable(this.weightMD.get(group)).orElse(1d) + en.getValue());
            }
        }
        if (anotherBonus.getTime() != 0) this.time += (anotherBonus.getTime() - 1);
        if (anotherBonus.getDoubleLoot() != 0) this.doubleLoot += anotherBonus.getDoubleLoot();
        if (anotherBonus.getDifficulty() != 0) this.difficulty += anotherBonus.getDifficulty();
        if (anotherBonus.getScore() != 0) this.score += (anotherBonus.getScore() - 1);
        if (anotherBonus.canLavaFishing()) this.canLavaFishing = true;
    }
}