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

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the basic settings for a game.
 */
public interface GameBasics {

    /**
     * Gets the time for the game.
     *
     * @return the minimum time in seconds.
     */
    MathValue<Player> time();

    /**
     * Gets the difficulty for the game.
     *
     * @return the minimum difficulty level.
     */
    MathValue<Player> difficulty();

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
     * @param context the context.
     * @param effect  the effect to apply.
     * @return the generated {@link GameSetting} instance.
     */
    @NotNull GameSetting toGameSetting(Context<Player> context, Effect effect);

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
        Builder difficulty(MathValue<Player> value);

        /**
         * Sets the time for the game.
         *
         * @param value the time in seconds.
         * @return the current {@link Builder} instance.
         */
        Builder time(MathValue<Player> value);

        /**
         * Builds and returns the {@link GameBasics} instance.
         *
         * @return the constructed {@link GameBasics} instance.
         */
        GameBasics build();
    }
}
