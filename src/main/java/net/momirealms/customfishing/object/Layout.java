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

    private String start;
    private String bar;
    private String pointer;
    private String offset;
    private String end;
    private String pointerOffset;
    private String title;

    public Layout(int range, double[] successRate, int size){
        this.range = range;
        this.successRate = successRate;
        this.size = size;
    }

    public void setBar(String bar) {this.bar = bar;}
    public void setEnd(String end) {this.end = end;}
    public void setOffset(String offset) {this.offset = offset;}
    public void setPointer(String pointer) {this.pointer = pointer;}
    public void setPointerOffset(String pointerOffset) {this.pointerOffset = pointerOffset;}
    public void setStart(String start) {this.start = start;}
    public void setTitle(String title) {this.title = title;}

    public int getRange(){return this.range;}
    public double[] getSuccessRate(){return this.successRate;}
    public int getSize(){return this.size;}
    public String getBar() {return bar;}
    public String getEnd() {return end;}
    public String getOffset() {return offset;}
    public String getPointer() {return pointer;}
    public String getPointerOffset() {return pointerOffset;}
    public String getStart() {return start;}
    public String getTitle() {return title;}
}
