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

package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.common.util.RandomUtils;
import org.jetbrains.annotations.NotNull;

public record GameBasicsImpl(int minTime, int maxTime, int minDifficulty, int maxDifficulty) implements GameBasics {

    public static class BuilderImpl implements Builder {
        private int minTime;
        private int maxTime;
        private int minDifficulty;
        private int maxDifficulty;
        @Override
        public Builder difficulty(int value) {
            minDifficulty = (maxDifficulty = value);
            return this;
        }
        @Override
        public Builder difficulty(int min, int max) {
            minDifficulty = min;
            maxDifficulty = max;
            return this;
        }
        @Override
        public Builder time(int value) {
            minTime = (maxTime = value);
            return this;
        }
        @Override
        public Builder time(int min, int max) {
            minTime = min;
            maxTime = max;
            return this;
        }
        @Override
        public GameBasics build() {
            return new GameBasicsImpl(minTime, maxTime, minDifficulty, maxDifficulty);
        }
    }

    @Override
    @NotNull
    public GameSetting toGameSetting(Effect effect) {
        return new GameSetting(
                RandomUtils.generateRandomInt(minTime, maxTime) * effect.gameTimeMultiplier() + effect.gameTimeAdder(),
                (int) Math.min(100, Math.max(1, RandomUtils.generateRandomInt(minDifficulty, maxDifficulty) * effect.difficultyMultiplier() + effect.difficultyAdder()))
        );
    }
}