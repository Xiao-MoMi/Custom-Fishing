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

import net.momirealms.customfishing.fishing.bar.FishingBar;

import java.util.Random;

public class MiniGameConfig {

    private final int time;
    private final FishingBar[] bars;
    private final int[] difficulties;

    public MiniGameConfig(int time, FishingBar[] bars, int[] difficulties) {
        this.time = time;
        this.bars = bars;
        this.difficulties = difficulties;
    }

    public FishingBar getRandomBar() {
        return bars[new Random().nextInt(bars.length)];
    }

    public int getRandomDifficulty() {
        return difficulties[new Random().nextInt(difficulties.length)];
    }

    public int getTime() {
        return this.time;
    }
}
