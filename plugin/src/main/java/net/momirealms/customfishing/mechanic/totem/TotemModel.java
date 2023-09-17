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

package net.momirealms.customfishing.mechanic.totem;

import net.momirealms.customfishing.mechanic.totem.block.TotemBlock;
import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.Axis;
import org.bukkit.Location;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;

public class TotemModel implements Serializable {

    private int coreX;
    private final int coreY;
    private int coreZ;
    private TotemBlock[][][][] model;

    public TotemModel(int coreX, int coreY, int coreZ, TotemBlock[][][][] model) {
        this.coreX = coreX;
        this.coreY = coreY;
        this.coreZ = coreZ;
        this.model = model;
    }

    public TotemBlock[] getTotemCore() {
        return model[coreY][coreZ][coreX];
    }

    public int getCoreX() {
        return coreX;
    }

    public int getCoreY() {
        return coreY;
    }

    public int getCoreZ() {
        return coreZ;
    }

    public TotemBlock[][][][] getModel() {
        return model;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int h = 0; h < model.length; h++) {
            stringBuilder.append("layer: ").append(h + 1).append("\n");
            TotemBlock[][][] totemBlocks1 = model[h];
            for (TotemBlock[][] totemBlocks2 : totemBlocks1) {
                for (TotemBlock[] totemBlocks3 : totemBlocks2) {
                    StringJoiner stringJoiner = new StringJoiner("||");
                    for (TotemBlock totemBlock : totemBlocks3) {
                        stringJoiner.add(totemBlock.toString());
                    }
                    stringBuilder.append(stringJoiner).append("\t");
                }
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public TotemModel clone() {
        return SerializationUtils.clone(this);
    }

    public TotemModel rotate90() {
        int tempX = this.coreX;
        this.coreX = this.coreZ;
        this.coreZ = this.model[0][0].length - 1 - tempX;
        this.model = rotate90(model);
        for (TotemBlock[][][] totemBlocks1 : model) {
            for (TotemBlock[][] totemBlocks2 : totemBlocks1) {
                for (TotemBlock[] totemBlocks3 : totemBlocks2) {
                    for (TotemBlock totemBlock : totemBlocks3) {
                        totemBlock.rotate90();
                    }
                }
            }
        }
        return this;
    }

    public TotemModel mirrorHorizontally() {
        mirrorHorizontally(model);
        this.coreZ = model[0].length - this.coreZ - 1;
        for (TotemBlock[][][] totemBlocks1 : model) {
            for (TotemBlock[][] totemBlocks2 : totemBlocks1) {
                for (TotemBlock[] totemBlocks3 : totemBlocks2) {
                    for (TotemBlock totemBlock : totemBlocks3) {
                        totemBlock.mirror(Axis.X);
                    }
                }
            }
        }
        return this;
    }

    public TotemModel mirrorVertically() {
        mirrorVertically(model);
        this.coreX = model[0][0].length - this.coreX - 1;

        for (TotemBlock[][][] totemBlocks1 : model) {
            for (TotemBlock[][] totemBlocks2 : totemBlocks1) {
                for (TotemBlock[] totemBlocks3 : totemBlocks2) {
                    for (TotemBlock totemBlock : totemBlocks3) {
                        totemBlock.mirror(Axis.Z);
                    }
                }
            }
        }
        return this;
    }

    public boolean isPatternSatisfied(Location location) {
        Location startLoc = location.clone().subtract(0, coreY, 0);

        int height = model.length;
        int width = model[0].length;
        int length = model[0][0].length;

        for (int y = 0; y < height; y++) {
            Location loc = startLoc.clone().add(-coreX, y, -coreZ);
            for (int z = 0; z < width; z++) {
                outer:
                    for (int x = 0; x < length; x++) {
                        for (TotemBlock totemBlock : model[y][z][x]) {
                            if (totemBlock.isRightBlock(loc.clone().add(x, 0, z).getBlock())) {
                                continue outer;
                            }
                        }
                        return false;
                    }
            }
        }
        return true;
    }

    private static TotemBlock[][][][] rotate90(TotemBlock[][][][] matrix) {
        int height = matrix.length;
        int rows = matrix[0].length;
        int cols = matrix[0][0].length;
        TotemBlock[][][][] rotated = new TotemBlock[height][cols][rows][];
        for (int h = 0; h < height; h++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    rotated[h][c][rows - 1 - r] = matrix[h][r][c];
                }
            }
        }
        return rotated;
    }

    private static void mirrorHorizontally(TotemBlock[][][][] matrix) {
        int height = matrix.length;
        int rows = matrix[0].length;
        int cols = matrix[0][0].length;

        for (int h = 0; h < height; h++) {
            for (int i = 0; i < rows / 2; i++) {
                for (int j = 0; j < cols; j++) {
                    TotemBlock[] temp = matrix[h][i][j];
                    matrix[h][i][j] = matrix[h][rows - i - 1][j];
                    matrix[h][rows - i - 1][j] = temp;
                }
            }
        }
    }

    private static void mirrorVertically(TotemBlock[][][][] matrix) {
        int height = matrix.length;
        int rows = matrix[0].length;
        int cols = matrix[0][0].length;
        for (int h = 0; h < height; h++) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols / 2; j++) {
                    TotemBlock[] temp = matrix[h][i][j];
                    matrix[h][i][j] = matrix[h][i][cols - j - 1];
                    matrix[h][i][cols - j - 1] = temp;
                }
            }
        }
    }
}
