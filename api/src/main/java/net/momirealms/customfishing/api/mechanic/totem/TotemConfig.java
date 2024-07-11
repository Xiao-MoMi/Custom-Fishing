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

package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Interface representing the configuration for a totem.
 * This interface provides methods for retrieving totem models, particle settings,
 * and other configuration details, as well as a builder for creating instances.
 */
public interface TotemConfig {

    /**
     * Retrieves the models for the totem.
     *
     * @return an array of {@link TotemModel} instances
     */
    TotemModel[] totemModels();

    /**
     * Retrieves the unique identifier for the totem configuration.
     *
     * @return the unique identifier as a String
     */
    String id();

    /**
     * Checks if the location matches the correct pattern for the totem.
     *
     * @param location the {@link Location} to be checked
     * @return true if the location matches the pattern, false otherwise
     */
    boolean isRightPattern(Location location);

    /**
     * Retrieves the particle settings for the totem.
     *
     * @return an array of {@link TotemParticle} instances
     */
    TotemParticle[] particleSettings();

    /**
     * Retrieves the radius of the totem's effect.
     *
     * @return the radius as a {@link MathValue} for {@link Player}
     */
    MathValue<Player> radius();

    /**
     * Retrieves the duration of the totem's effect.
     *
     * @return the duration as a {@link MathValue} for {@link Player}
     */
    MathValue<Player> duration();

    /**
     * Retrieves the core blocks of the totem.
     *
     * @return an array of {@link TotemBlock} instances
     */
    TotemBlock[] totemCore();

    /**
     * Creates a new {@link Builder} instance to construct a {@link TotemConfig}.
     *
     * @return a new {@link Builder} instance
     */
    static Builder builder() {
        return new TotemConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing instances of {@link TotemConfig}.
     */
    interface Builder {

        /**
         * Sets the unique identifier for the {@link TotemConfig} being built.
         *
         * @param id the unique identifier as a String
         * @return the {@link Builder} instance for method chaining
         */
        Builder id(String id);

        /**
         * Sets the totem models for the {@link TotemConfig} being built.
         *
         * @param totemModels an array of {@link TotemModel} instances
         * @return the {@link Builder} instance for method chaining
         */
        Builder totemModels(TotemModel[] totemModels);

        /**
         * Sets the particle settings for the {@link TotemConfig} being built.
         *
         * @param particleSettings an array of {@link TotemParticle} instances
         * @return the {@link Builder} instance for method chaining
         */
        Builder particleSettings(TotemParticle[] particleSettings);

        /**
         * Sets the radius of the totem's effect for the {@link TotemConfig} being built.
         *
         * @param radius the radius as a {@link MathValue} for {@link Player}
         * @return the {@link Builder} instance for method chaining
         */
        Builder radius(MathValue<Player> radius);

        /**
         * Sets the duration of the totem's effect for the {@link TotemConfig} being built.
         *
         * @param duration the duration as a {@link MathValue} for {@link Player}
         * @return the {@link Builder} instance for method chaining
         */
        Builder duration(MathValue<Player> duration);

        /**
         * Builds and returns a new {@link TotemConfig} instance.
         *
         * @return a new {@link TotemConfig} instance
         */
        TotemConfig build();
    }
}
