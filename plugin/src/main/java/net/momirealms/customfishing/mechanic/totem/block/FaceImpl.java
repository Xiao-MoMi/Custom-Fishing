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
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class FaceImpl implements TotemBlockProperty {

    private final BlockFace blockFace;

    public FaceImpl(BlockFace blockFace) {
        this.blockFace = blockFace;
    }

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
            case EAST -> {
                return new FaceImpl(BlockFace.SOUTH);
            }
            case SOUTH -> {
                return new FaceImpl(BlockFace.WEST);
            }
            case WEST -> {
                return new FaceImpl(BlockFace.NORTH);
            }
            case NORTH -> {
                return new FaceImpl(BlockFace.EAST);
            }
            default -> throw new IllegalArgumentException("Unsupported block facing: " + blockFace);
        }
    }

    @Override
    public boolean isPropertyMet(Block block) {
        if (block.getBlockData() instanceof Directional directional) {
            return directional.getFacing().equals(this.blockFace);
        }
        return false;
    }
}
