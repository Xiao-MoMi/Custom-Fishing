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
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import java.io.Serializable;
import java.util.Locale;

public class FaceImpl implements TotemBlockProperty, Serializable {

    private BlockFace blockFace;

    public FaceImpl(BlockFace blockFace) {
        this.blockFace = blockFace;
    }

    /**
     * Mirrors the block face if the axis is X or Z.
     * @param axis The axis to mirror.
     * @return The mirrored block face.
     */
    @Override
    public TotemBlockProperty mirror(Axis axis) {
        if (axis == Axis.X) {
            if (blockFace == BlockFace.SOUTH || blockFace == BlockFace.NORTH) {
                return new FaceImpl(blockFace.getOppositeFace());
            } else {
                return this;
            }
        } else if (axis == Axis.Z) {
            if (blockFace == BlockFace.EAST || blockFace == BlockFace.WEST) {
                return new FaceImpl(blockFace.getOppositeFace());
            } else {
                return this;
            }
        }
        return this;
    }

    @Override
    public TotemBlockProperty rotate90() {
        switch (blockFace) {
            case UP, DOWN -> {
                return this;
            }
            case EAST -> blockFace = BlockFace.SOUTH;
            case SOUTH -> blockFace = BlockFace.WEST;
            case WEST -> blockFace = BlockFace.NORTH;
            case NORTH -> blockFace = BlockFace.EAST;
            default -> throw new IllegalArgumentException("Unsupported block facing: " + blockFace);
        }
        return this;
    }

    @Override
    public boolean isPropertyMet(Block block) {
        if (block.getBlockData() instanceof Directional directional) {
            return directional.getFacing().equals(this.blockFace);
        }
        return false;
    }

    @Override
    public String getRawText() {
        return "face=" + blockFace.name().toLowerCase(Locale.ENGLISH);
    }
}
