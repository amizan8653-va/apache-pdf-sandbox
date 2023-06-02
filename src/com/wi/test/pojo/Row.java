package com.wi.test.pojo;

import java.util.List;

public class Row {
    private final List<Cell> cells;
    private final float height;
    public Row(List<Cell> cells, float height) {
        this.height = height;
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


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "{ \n");
        sb.append("\t\"cells\" : [ ");
        for (Cell c : cells) {
            sb.append(c.toString().replace("\n\t", "\n\t\t").replace("\n}", "\n\t}"));
            sb.append(", ");
        }
        sb.deleteCharAt(sb.lastIndexOf(", "));
        sb.append( "]\n");
        sb.append("}");
        return sb.toString();
    }
}
