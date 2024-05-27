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

package net.momirealms.customfishing.api.mechanic.totem.block.property;

import org.bukkit.Axis;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;

import java.io.Serializable;
import java.util.Locale;

public class AxisImpl implements TotemBlockProperty, Serializable {

    private Axis axis;

    public AxisImpl(Axis axis) {
        this.axis = axis;
    }

    @Override
    public TotemBlockProperty mirror(Axis axis) {
        return this;
    }

    /**
     * Rotates the block axis 90 degrees. (X -> Z, Z -> X)
     * @return The rotated block axis.
     */
    @Override
    public TotemBlockProperty rotate90() {
        if (this.axis == Axis.X) {
            axis = Axis.Z;
        } else if (this.axis == Axis.Z) {
            axis = Axis.X;
        }
        return this;
    }

    /**
     * Checks if the block has the property.
     * @param block The block to check.
     * @return True if the block has the property.
     */
    @Override
    public boolean isPropertyMet(Block block) {
        if (block.getBlockData() instanceof Orientable orientable) {
            return orientable.getAxis().equals(this.axis);
        }
        return false;
    }

    @Override
    public String getRawText() {
        return "axis=" + axis.name().toLowerCase(Locale.ENGLISH);
    }
}
