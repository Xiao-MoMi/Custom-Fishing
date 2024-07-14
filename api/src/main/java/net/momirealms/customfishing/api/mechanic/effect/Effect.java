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

package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Represents an effect applied in the fishing.
 */
public interface Effect {

    /**
     * Retrieves the properties of this effect.
     *
     * @return a map of effect properties and their values
     */
    Map<EffectProperties<?>, Object> properties();

    /**
     * Put the properties into the effect
     *
     * @param properties properties to add
     * @return the effect instance
     */
    Effect properties(Map<EffectProperties<?>, Object> properties);

    /**
     * Sets the specified property to the given value.
     *
     * @param key the property key
     * @param value the property value
     * @param <C> the type of the property value
     * @return the effect instance with the updated property
     */
    <C> EffectImpl arg(EffectProperties<C> key, C value);

    /**
     * Retrieves the value of the specified property.
     *
     * @param key the property key
     * @param <C> the type of the property value
     * @return the value of the specified property
     */
    <C> C arg(EffectProperties<C> key);

    /**
     * Gets the chance of multiple loots.
     *
     * @return the multiple loot chance
     */
    double multipleLootChance();

    /**
     * Sets the chance of multiple loots.
     *
     * @param multipleLootChance the new multiple loot chance
     * @return the effect instance
     */
    Effect multipleLootChance(double multipleLootChance);

    /**
     * Gets the size adder.
     *
     * @return the size adder
     */
    double sizeAdder();

    /**
     * Sets the size adder.
     *
     * @param sizeAdder the new size adder
     * @return the effect instance
     */
    Effect sizeAdder(double sizeAdder);

    /**
     * Gets the size multiplier.
     *
     * @return the size multiplier
     */
    double sizeMultiplier();

    /**
     * Sets the size multiplier.
     *
     * @param sizeMultiplier the new size multiplier
     * @return the effect instance
     */
    Effect sizeMultiplier(double sizeMultiplier);

    /**
     * Gets the score adder.
     *
     * @return the score adder
     */
    double scoreAdder();

    /**
     * Sets the score adder.
     *
     * @param scoreAdder the new score adder
     * @return the effect instance
     */
    Effect scoreAdder(double scoreAdder);

    /**
     * Gets the score multiplier.
     *
     * @return the score multiplier
     */
    double scoreMultiplier();

    /**
     * Sets the score multiplier.
     *
     * @param scoreMultiplier the new score multiplier
     * @return the effect instance
     */
    Effect scoreMultiplier(double scoreMultiplier);

    /**
     * Gets the wait time adder.
     *
     * @return the wait time adder
     */
    double waitTimeAdder();

    /**
     * Sets the wait time adder.
     *
     * @param waitTimeAdder the new wait time adder
     * @return the effect instance
     */
    Effect waitTimeAdder(double waitTimeAdder);

    /**
     * Gets the wait time multiplier.
     *
     * @return the wait time multiplier
     */
    double waitTimeMultiplier();

    /**
     * Sets the wait time multiplier.
     *
     * @param waitTimeMultiplier the new wait time multiplier
     * @return the effect instance
     */
    Effect waitTimeMultiplier(double waitTimeMultiplier);

    /**
     * Gets the game time adder.
     *
     * @return the game time adder
     */
    double gameTimeAdder();

    /**
     * Sets the game time adder.
     *
     * @param gameTimeAdder the new game time adder
     * @return the effect instance
     */
    Effect gameTimeAdder(double gameTimeAdder);

    /**
     * Gets the game time multiplier.
     *
     * @return the game time multiplier
     */
    double gameTimeMultiplier();

    /**
     * Sets the game time multiplier.
     *
     * @param gameTimeMultiplier the new game time multiplier
     * @return the effect instance
     */
    Effect gameTimeMultiplier(double gameTimeMultiplier);

    /**
     * Gets the difficulty adder.
     *
     * @return the difficulty adder
     */
    double difficultyAdder();

    /**
     * Sets the difficulty adder.
     *
     * @param difficultyAdder the new difficulty adder
     * @return the effect instance
     */
    Effect difficultyAdder(double difficultyAdder);

    /**
     * Gets the difficulty multiplier.
     *
     * @return the difficulty multiplier
     */
    double difficultyMultiplier();

    /**
     * Sets the difficulty multiplier.
     *
     * @param difficultyMultiplier the new difficulty multiplier
     * @return the effect instance
     */
    Effect difficultyMultiplier(double difficultyMultiplier);

    /**
     * Gets the list of weight operations.
     *
     * @return the list of weight operations
     */
    List<Pair<String, BiFunction<Context<Player>, Double, Double>>> weightOperations();

    /**
     * Adds the list of weight operations.
     *
     * @param weightOperations the list of weight operations to add
     * @return the effect instance
     */
    Effect weightOperations(List<Pair<String, BiFunction<Context<Player>, Double, Double>>> weightOperations);

    /**
     * Gets the list of weight operations that are conditions ignored.
     *
     * @return the list of weight operations that are conditions ignored
     */
    List<Pair<String, BiFunction<Context<Player>, Double, Double>>> weightOperationsIgnored();

    /**
     * Adds the list of weight operations that are conditions ignored.
     *
     * @param weightOperations the list of weight operations that are conditions ignored
     * @return the effect instance
     */
    Effect weightOperationsIgnored(List<Pair<String, BiFunction<Context<Player>, Double, Double>>> weightOperations);

    /**
     * Combines this effect with another effect.
     *
     * @param effect the effect to combine with
     */
    void combine(Effect effect);

    /**
     * Get a copy of the effect
     *
     * @return the copied effect
     */
    Effect copy();

    /**
     * Creates a new instance of {@link Effect}.
     *
     * @return a new {@link Effect} instance
     */
    static Effect newInstance() {
        return new EffectImpl();
    }
}
