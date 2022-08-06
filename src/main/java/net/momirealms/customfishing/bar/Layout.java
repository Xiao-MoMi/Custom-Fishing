package net.momirealms.customfishing.titlebar;

public class Layout {

    private final String key;
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

    public Layout(String key, int range, double[] successRate, int size){
        this.key = key;
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
