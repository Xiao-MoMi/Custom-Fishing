package net.momirealms.customfishing.utils;

public class VectorUtil {

    private final double horizontal;
    private final double vertical;

    public VectorUtil(double horizontal, double vertical){
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public double getHorizontal(){return this.horizontal;}
    public double getVertical(){return this.vertical;}
}
