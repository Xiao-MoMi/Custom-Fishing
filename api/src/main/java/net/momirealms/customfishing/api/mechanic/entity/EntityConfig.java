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

package net.momirealms.customfishing.api.mechanic.entity;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The EntityConfig interface defines the configuration for an entity.
 * It includes methods to retrieve various properties of the entity such as vectors and an ID, as well
 * as a nested Builder interface for constructing instances of EntityConfig.
 */
public interface EntityConfig {

    MathValue<Player> DEFAULT_HORIZONTAL_VECTOR = MathValue.plain(1.1);
    MathValue<Player> DEFAULT_VERTICAL_VECTOR = MathValue.plain(1.2);
    String DEFAULT_ENTITY_ID = "COD";
    Map<String, Object> DEFAULT_PROPERTY_MAP = Map.of();

    /**
     * Gets the ID
     *
     * @return the ID.
     */
    String id();

    /**
     * Retrieves the horizontal vector value for the entity.
     *
     * @return the horizontal vector value as a double
     */
    MathValue<Player> horizontalVector();

    /**
     * Retrieves the vertical vector value for the entity.
     *
     * @return the vertical vector value as a double
     */
    MathValue<Player> verticalVector();

    /**
     * Retrieves the unique identifier for the entity.
     *
     * @return the entity ID as a non-null String
     */
    @NotNull
    String entityID();

    /**
     * Retrieves a map of properties associated with the entity.
     *
     * @return a non-null map where keys are property names and values are property values
     */
    @NotNull
    Map<String, Object> propertyMap();

    /**
     * Creates a new Builder instance for constructing an EntityConfig.
     *
     * @return a new Builder instance
     */
    static Builder builder() {
        return new EntityConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing instances of EntityConfig.
     */
    interface Builder {

        /**
         * Sets the ID
         *
         * @return the current Builder instance
         */
        Builder id(String id);

        /**
         * Sets the entity ID for the EntityConfig being built.
         *
         * @param value the entity ID as a String
         * @return the current Builder instance
         */
        Builder entityID(String value);

        /**
         * Sets the vertical vector value for the EntityConfig being built.
         *
         * @param value the vertical vector value as a double
         * @return the current Builder instance
         */
        Builder verticalVector(MathValue<Player> value);

        /**
         * Sets the horizontal vector value for the EntityConfig being built.
         *
         * @param value the horizontal vector value as a double
         * @return the current Builder instance
         */
        Builder horizontalVector(MathValue<Player> value);

        /**
         * Sets the property map for the EntityConfig being built.
         *
         * @param value a map of properties where keys are property names and values are property values
         * @return the current Builder instance
         */
        Builder propertyMap(Map<String, Object> value);

        /**
         * Builds and returns the EntityConfig instance based on the current state of the Builder.
         *
         * @return a new EntityConfig instance
         */
        EntityConfig build();
    }
}
