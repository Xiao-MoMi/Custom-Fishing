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

package net.momirealms.customfishing.object;

public class Layout {

    private final int range;
    private final double[] successRate;
    private final int size;
    private final String start;
    private final String bar;
    private final String pointer;
    private final String offset;
    private final String end;
    private final String pointerOffset;
    private final String title;

    public Layout(int range, double[] successRate, int size, String start, String bar, String pointer, String offset, String end, String pointerOffset, String title) {
        this.range = range;
        this.successRate = successRate;
        this.size = size;
        this.start = start;
        this.bar = bar;
        this.pointer = pointer;
        this.offset = offset;
        this.end = end;
        this.pointerOffset = pointerOffset;
        this.title = title;
    }

    public int getRange() {
        return range;
    }

    public double[] getSuccessRate() {
        return successRate;
    }

    public int getSize() {
        return size;
    }

    public String getStart() {
        return start;
    }

    public String getBar() {
        return bar;
    }

    public String getPointer() {
        return pointer;
    }

    public String getOffset() {
        return offset;
    }

    public String getEnd() {
        return end;
    }

    public String getPointerOffset() {
        return pointerOffset;
    }

    public String getTitle() {
        return title;
    }
}
