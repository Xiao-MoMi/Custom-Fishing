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

package net.momirealms.customfishing.mechanic.totem.block.property;

import org.bukkit.Axis;
import org.bukkit.block.Block;

public interface TotemBlockProperty {

    /**
     * Mirrors the block face if the axis is X or Z.
     * @param axis The axis to mirror.
     * @return The mirrored block face.
     */
    TotemBlockProperty mirror(Axis axis);

    /**
     * Rotates the block face 90 degrees.
     * @return The rotated block face.
     */
    TotemBlockProperty rotate90();

    /**
     * Checks if the block has the property.
     * @param block The block to check.
     * @return True if the block has the property.
     */
    boolean isPropertyMet(Block block);

    /**
     * Gets the raw text of the property.
     * @return The raw text of the property.
     */
    String getRawText();
}
