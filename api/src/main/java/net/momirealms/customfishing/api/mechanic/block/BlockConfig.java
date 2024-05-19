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

package net.momirealms.customfishing.api.mechanic.block;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface representing the configuration for a custom block in the CustomFishing plugin.
 * Provides methods to access various block properties and modifiers.
 */
public interface BlockConfig {

    /**
     * Gets the unique identifier for the block.
     *
     * @return The block's unique identifier.
     */
    String blockID();

    /**
     * Gets the list of data modifiers applied to the block.
     *
     * @return A list of {@link BlockDataModifier} objects.
     */
    List<BlockDataModifier> dataModifier();

    /**
     * Gets the list of state modifiers applied to the block.
     *
     * @return A list of {@link BlockStateModifier} objects.
     */
    List<BlockStateModifier> stateModifiers();

    /**
     * Gets the horizontal vector math value associated with the block.
     *
     * @return A {@link MathValue} representing the horizontal vector for a player.
     */
    MathValue<Player> horizontalVector();

    /**
     * Gets the vertical vector math value associated with the block.
     *
     * @return A {@link MathValue} representing the vertical vector for a player.
     */
    MathValue<Player> verticalVector();

    /**
     * Creates a new builder instance for constructing a {@link BlockConfig}.
     *
     * @return A new {@link Builder} instance.
     */
    static Builder builder() {
        return new BlockConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing a {@link BlockConfig} instance.
     */
    interface Builder {

        /**
         * Sets the block ID for the configuration.
         *
         * @param blockID The block's unique identifier.
         * @return The current {@link Builder} instance.
         */
        Builder blockID(String blockID);

        /**
         * Sets the list of data modifiers for the configuration.
         *
         * @param dataModifierList A list of {@link BlockDataModifier} objects.
         * @return The current {@link Builder} instance.
         */
        Builder dataModifierList(List<BlockDataModifier> dataModifierList);

        /**
         * Sets the list of state modifiers for the configuration.
         *
         * @param stateModifierList A list of {@link BlockStateModifier} objects.
         * @return The current {@link Builder} instance.
         */
        Builder stateModifierList(List<BlockStateModifier> stateModifierList);

        /**
         * Sets the horizontal vector math value for the configuration.
         *
         * @param horizontalVector A {@link MathValue} representing the horizontal vector for a player.
         * @return The current {@link Builder} instance.
         */
        Builder horizontalVector(MathValue<Player> horizontalVector);

        /**
         * Sets the vertical vector math value for the configuration.
         *
         * @param verticalVector A {@link MathValue} representing the vertical vector for a player.
         * @return The current {@link Builder} instance.
         */
        Builder verticalVector(MathValue<Player> verticalVector);

        /**
         * Builds and returns the configured {@link BlockConfig} instance.
         *
         * @return The constructed {@link BlockConfig} instance.
         */
        BlockConfig build();
    }
}