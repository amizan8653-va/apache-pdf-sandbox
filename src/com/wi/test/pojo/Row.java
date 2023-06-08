package com.wi.test.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class Row {
    private final List<Cell> cells;

    @Setter
    private float height;
    public Row(List<Cell> cells) {
        this.height = 0;
        this.cells = cells;
    }

    public float getCellPosition(int cellIndex) {
        float currentPosition = 0;
        for (int i = 0; i < cellIndex; i++) {
            currentPosition += cells.get(i).getWidth();
        }
        return currentPosition;
    }
}
