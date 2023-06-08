package com.wi.test.pojo;

import java.util.List;

public class Row {
    private final List<Cell> cells;
    private float height;
    public Row(List<Cell> cells) {
        this.height = 0;
        this.cells = cells;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public float getCellPosition(int cellIndex) {
        float currentPosition = 0;
        for (int i = 0; i < cellIndex; i++) {
            currentPosition += cells.get(i).getWidth();
        }
        return currentPosition;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float newHeight) {
        height = newHeight;
    }
}
