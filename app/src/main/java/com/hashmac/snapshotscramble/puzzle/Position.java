package com.hashmac.snapshotscramble.puzzle;

public class Position {
    int xAxis;
    int yAxis;
    public Position(int xAxis, int yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }
    public int getXAxis() {
        return xAxis;
    }
    public void setXAxis(int xAxis) {
        this.xAxis = xAxis;
    }
    public int getYAxis() {
        return yAxis;
    }
    public void setYAxis(int yAxis) {
        this.yAxis = yAxis;
    }
}