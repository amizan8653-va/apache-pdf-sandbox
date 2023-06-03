package com.wi.test.pojo;

import java.util.ArrayList;
import java.util.List;

public class DataTable {

    private final List<Row> rows = new ArrayList<>();
    private final String summary;

    public DataTable(String summary) {
        this.summary = summary;
    }

    public void addRow(Row row) {
        this.rows.add(row);
    }

    public Cell getCell(int row, int col) {
        return rows.get(row).getCells().get(col);
    }

    public float getRowPosition(int rowIndex) {
        float currentPosition = 0;
        for (int i = 0; i < rowIndex; i++) {
            currentPosition += rows.get(i).getHeight();
        }
        return currentPosition;
    }

    public List<Row> getRows() {
        return rows;
    }

    public String getSummary() {
        return summary;
    }

}
