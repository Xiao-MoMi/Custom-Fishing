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


package net.momirealms.customfishing.fishing.totem;

import java.util.Arrays;

public class OriginalModel {

    private final int length;
    private final int width;
    private final int height;
    private final String[][][][] model;
    private CorePos corePos;

    public OriginalModel(int length, int width, int height) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.model = new String[length][width][height][];
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("TotemModel:");
        for (int i = 0; i < height; i++) {
            stringBuilder.append("\nlayer: ").append(i + 1);
            for (int j = 0; j < width; j++) {
                stringBuilder.append("\n");
                for (int k = 0; k < height; k++) {
                    stringBuilder.append(Arrays.toString(model[k][j][i])).append("\t");
                }
            }
        }
        return stringBuilder.toString();
    }

    public CorePos getCorePos() {
        return corePos;
    }

    public void setCorePos(CorePos corePos) {
        this.corePos = corePos;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setElement(String[] element, int length, int width, int height) {
        this.model[length][width][height] = element;
    }

    public String[] getElement(int length, int width, int height) {
        return this.model[length][width][height];
    }
}
