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

package net.momirealms.customfishing.mechanic.totem.block;

import org.bukkit.Axis;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;

public class AxisImpl implements TotemBlockProperty {

    private final Axis axis;

    public AxisImpl(Axis axis) {
        this.axis = axis;
    }

    @Override
    public TotemBlockProperty mirror(Axis axis) {
        return this;
    }

    @Override
    public TotemBlockProperty rotate90() {
        if (this.axis == Axis.Y) {
            return this;
        } else if (this.axis == Axis.X) {
            return new AxisImpl(Axis.Z);
        } else {
            return new AxisImpl(Axis.X);
        }
    }

    @Override
    public boolean isPropertyMet(Block block) {
        if (block.getBlockData() instanceof Orientable orientable) {
            return orientable.getAxis().equals(this.axis);
        }
        return false;
    }
}
