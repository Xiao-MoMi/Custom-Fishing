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
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class BasicGameConfig {

    private int minTime;
    private int maxTime;
    private int minDifficulty;
    private int maxDifficulty;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final BasicGameConfig basicGameConfig;

        public Builder() {
            basicGameConfig = new BasicGameConfig();
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder difficulty(int value) {
            basicGameConfig.minDifficulty = (basicGameConfig.maxDifficulty = value);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder difficulty(int min, int max) {
            basicGameConfig.minDifficulty = min;
            basicGameConfig.maxDifficulty = max;
            return this;
        }

        public Builder time(int value) {
            basicGameConfig.minTime = (basicGameConfig.maxTime = value);
            return this;
        }

        public Builder time(int min, int max) {
            basicGameConfig.minTime = min;
            basicGameConfig.maxTime = max;
            return this;
        }

        public BasicGameConfig build() {
            return basicGameConfig;
        }
    }

    /**
     * Generates random game settings based on specified time and difficulty ranges, adjusted by an effect's difficulty modifier.
     *
     * @param effect The effect to adjust the difficulty.
     * @return A {@link GameSettings} object representing the generated game settings.
     */
    @Nullable
    public GameSettings getGameSetting(Effect effect) {
        return new GameSettings(
                ThreadLocalRandom.current().nextInt(minTime, maxTime + 1) * effect.getGameTimeMultiplier() + effect.getGameTime(),
                (int) Math.min(100, Math.max(1, ThreadLocalRandom.current().nextInt(minDifficulty, maxDifficulty + 1) * effect.getDifficultyMultiplier() + effect.getDifficulty()))
        );
    }
}