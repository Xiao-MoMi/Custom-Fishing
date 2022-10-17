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

public class Modifier {

    private int difficulty;
    private double score;
    private boolean willDouble;

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setWillDouble(boolean willDouble) {
        this.willDouble = willDouble;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public double getScore() {
        return score;
    }

    public boolean isWillDouble() {
        return willDouble;
    }
}