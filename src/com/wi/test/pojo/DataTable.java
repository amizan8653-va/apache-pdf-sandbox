package com.wi.test.pojo;

import java.util.ArrayList;
import java.util.List;

public class DataTable {

    private final List<Row> rows = new ArrayList<>();
    private final String summary;
    private final String id;

    public DataTable(String summary, String id) {
        this.summary = summary;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "{ \n");
        sb.append("\t\"rows\" : [ ");
        for (Row r : rows) {
            sb.append(r.toString().replace("\n\t", "\n\t\t").replace("\n}", "\n\t}"));
            sb.append(", ");
        }
        sb.deleteCharAt(sb.lastIndexOf(", "));
        sb.append( "]\n");
        sb.append("}");
        return sb.toString();
    }
}
