package com.wi.test.pojo;

import com.wi.test.enums.TableHeaderType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DataTable {

    private final List<Row> rows = new ArrayList<>();
    private final String summary;
    private final TableHeaderType tableHeaderType;

    public DataTable(String summary, TableHeaderType tableHeaderType) {
        this.summary = summary;
        this.tableHeaderType = tableHeaderType;
    }

    public void addRow(Row row) {
        this.rows.add(row);
    }

    public Cell getCell(int row, int col) {
        return rows.get(row).getCells().get(col);
    }

    public float getRowPosition(int rowIndexStart, int rowIndexEnd) {
        float currentPosition = 0;
        for (int i = rowIndexStart; i < rowIndexEnd; i++) {
            currentPosition += rows.get(i).getHeight();
        }
        return currentPosition;
    }

    public TableHeaderType getTableHeaderType() {
        return tableHeaderType;
    }
}
