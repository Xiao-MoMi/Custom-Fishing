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

package net.momirealms.customfishing.api.mechanic.totem.block;

import net.momirealms.customfishing.api.mechanic.totem.block.property.TotemBlockProperty;
import net.momirealms.customfishing.api.mechanic.totem.block.type.TypeCondition;
import org.bukkit.Axis;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * Represents a TotemBlock that defines conditions and properties for a specific block type in a totem structure.
 */
public class TotemBlock implements Serializable {

    private final TypeCondition typeCondition;
    private final TotemBlockProperty[] properties;

    /**
     * Initializes a TotemBlock with the specified TypeCondition and properties.
     *
     * @param typeCondition The TypeCondition that specifies the block type.
     * @param properties    An array of TotemBlockProperty objects representing additional block properties.
     */
    public TotemBlock(TypeCondition typeCondition, TotemBlockProperty[] properties) {
        this.typeCondition = typeCondition;
        this.properties = properties;
    }

    /**
     * Gets the TypeCondition associated with this TotemBlock.
     *
     * @return The TypeCondition defining the block type.
     */
    public TypeCondition getTypeCondition() {
        return typeCondition;
    }

    /**
     * Gets an array of properties associated with this TotemBlock.
     *
     * @return An array of TotemBlockProperty objects representing block properties.
     */
    public TotemBlockProperty[] getProperties() {
        return properties;
    }

    /**
     * Checks if a given Block satisfies the TypeCondition and properties of this TotemBlock.
     *
     * @param block The Block to be checked against the conditions and properties.
     * @return `true` if the block satisfies all conditions and properties, otherwise `false`.
     */
    public boolean isRightBlock(Block block) {
        if (!typeCondition.isMet(block)) {
            return false;
        }
        for (TotemBlockProperty property : properties) {
            if (!property.isPropertyMet(block)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Rotates the properties of this TotemBlock by 90 degrees.
     * This method should be called when the totem structure is rotated.
     */
    public void rotate90() {
        for (TotemBlockProperty property : properties) {
            property.rotate90();
        }
    }

    /**
     * Mirrors the properties of this TotemBlock horizontally or vertically.
     * This method should be called when the totem structure is mirrored.
     *
     * @param axis The Axis along which to mirror the properties (X or Z).
     */
    public void mirror(Axis axis) {
        for (TotemBlockProperty property : properties) {
            property.mirror(axis);
        }
    }

    /**
     * Returns the raw text representation of this TotemBlock, including its TypeCondition and properties.
     *
     * @return The raw text representation of this TotemBlock.
     */
    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(";");
        for (TotemBlockProperty property : properties) {
            stringJoiner.add(property.getRawText());
        }
        return typeCondition.getRawText() + "{" + stringJoiner + "}";
    }
}
