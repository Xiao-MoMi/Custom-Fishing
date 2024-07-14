/*
 *  Copyright (C) <2024> <XiaoMoMi>
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
import org.jetbrains.annotations.NotNull;

/**
 * Represents the basic settings for a game.
 */
public interface GameBasics {

    /**
     * Gets the minimum time for the game.
     *
     * @return the minimum time in seconds.
     */
    int minTime();

    /**
     * Gets the maximum time for the game.
     *
     * @return the maximum time in seconds.
     */
    int maxTime();

    /**
     * Gets the minimum difficulty for the game.
     *
     * @return the minimum difficulty level.
     */
    int minDifficulty();

    /**
     * Gets the maximum difficulty for the game.
     *
     * @return the maximum difficulty level.
     */
    int maxDifficulty();

    /**
     * Creates a new builder for constructing {@link GameBasics} instances.
     *
     * @return a new {@link Builder} instance.
     */
    static GameBasics.Builder builder() {
        return new GameBasicsImpl.BuilderImpl();
    }

    /**
     * Converts the game basics to a {@link GameSetting} instance based on the provided effect.
     *
     * @param effect the effect to apply.
     * @return the generated {@link GameSetting} instance.
     */
    @NotNull
    GameSetting toGameSetting(Effect effect);

    /**
     * Builder interface for constructing {@link GameBasics} instances.
     */
    interface Builder {

        /**
         * Sets the difficulty for the game.
         *
         * @param value the difficulty level.
         * @return the current {@link Builder} instance.
         */
        Builder difficulty(int value);

        /**
         * Sets the difficulty range for the game.
         *
         * @param min the minimum difficulty level.
         * @param max the maximum difficulty level.
         * @return the current {@link Builder} instance.
         */
        Builder difficulty(int min, int max);

        /**
         * Sets the time for the game.
         *
         * @param value the time in seconds.
         * @return the current {@link Builder} instance.
         */
        Builder time(int value);

        /**
         * Sets the time range for the game.
         *
         * @param min the minimum time in seconds.
         * @param max the maximum time in seconds.
         * @return the current {@link Builder} instance.
         */
        Builder time(int min, int max);

        /**
         * Builds and returns the {@link GameBasics} instance.
         *
         * @return the constructed {@link GameBasics} instance.
         */
        GameBasics build();
    }
}
