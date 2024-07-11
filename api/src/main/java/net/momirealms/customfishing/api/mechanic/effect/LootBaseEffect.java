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

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;

/**
 * Represents the base effect applied to loot.
 */
public interface LootBaseEffect {

    MathValue<Player> DEFAULT_WAIT_TIME_ADDER = MathValue.plain(0);
    MathValue<Player> DEFAULT_WAIT_TIME_MULTIPLIER = MathValue.plain(1);
    MathValue<Player> DEFAULT_DIFFICULTY_ADDER = MathValue.plain(0);
    MathValue<Player> DEFAULT_DIFFICULTY_MULTIPLIER = MathValue.plain(1);
    MathValue<Player> DEFAULT_GAME_TIME_ADDER = MathValue.plain(0);
    MathValue<Player> DEFAULT_GAME_TIME_MULTIPLIER = MathValue.plain(1);

    /**
     * Gets the adder value for wait time.
     *
     * @return the wait time adder value
     */
    MathValue<Player> waitTimeAdder();

    /**
     * Gets the multiplier value for wait time.
     *
     * @return the wait time multiplier value
     */
    MathValue<Player> waitTimeMultiplier();

    /**
     * Gets the adder value for difficulty.
     *
     * @return the difficulty adder value
     */
    MathValue<Player> difficultyAdder();

    /**
     * Gets the multiplier value for difficulty.
     *
     * @return the difficulty multiplier value
     */
    MathValue<Player> difficultyMultiplier();

    /**
     * Gets the adder value for game time.
     *
     * @return the game time adder value
     */
    MathValue<Player> gameTimeAdder();

    /**
     * Gets the multiplier value for game time.
     *
     * @return the game time multiplier value
     */
    MathValue<Player> gameTimeMultiplier();

    /**
     * Creates a new {@link Builder} instance for constructing {@link LootBaseEffect} objects.
     *
     * @return a new {@link Builder} instance
     */
    static Builder builder() {
        return new LootBaseEffectImpl.BuilderImpl();
    }

    /**
     * Convert the base effect to an effect instance
     *
     * @param context player context
     * @return the effect instance
     */
    Effect toEffect(Context<Player> context);

    /**
     * Builder interface for constructing {@link LootBaseEffect} instances.
     */
    interface Builder {

        /**
         * Sets the adder value for wait time.
         *
         * @param waitTimeAdder the wait time adder value
         * @return the builder instance
         */
        Builder waitTimeAdder(MathValue<Player> waitTimeAdder);

        /**
         * Sets the multiplier value for wait time.
         *
         * @param waitTimeMultiplier the wait time multiplier value
         * @return the builder instance
         */
        Builder waitTimeMultiplier(MathValue<Player> waitTimeMultiplier);

        /**
         * Sets the adder value for difficulty.
         *
         * @param difficultyAdder the difficulty adder value
         * @return the builder instance
         */
        Builder difficultyAdder(MathValue<Player> difficultyAdder);

        /**
         * Sets the multiplier value for difficulty.
         *
         * @param difficultyMultiplier the difficulty multiplier value
         * @return the builder instance
         */
        Builder difficultyMultiplier(MathValue<Player> difficultyMultiplier);

        /**
         * Sets the adder value for game time.
         *
         * @param gameTimeAdder the game time adder value
         * @return the builder instance
         */
        Builder gameTimeAdder(MathValue<Player> gameTimeAdder);

        /**
         * Sets the multiplier value for game time.
         *
         * @param gameTimeMultiplier the game time multiplier value
         * @return the builder instance
         */
        Builder gameTimeMultiplier(MathValue<Player> gameTimeMultiplier);

        /**
         * Builds and returns the {@link LootBaseEffect} instance.
         *
         * @return the built {@link LootBaseEffect} instance
         */
        LootBaseEffect build();
    }
}
