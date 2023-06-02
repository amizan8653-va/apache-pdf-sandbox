package com.wi.test.pojo;

public class TableCellMarkup {

    private final boolean header;
    private final String id;
    private final String scope;
    private final String[] headers;
    private final int rowSpan;
    private final int colspan;

    public TableCellMarkup() {
        this.scope = "";
        this.id = "";
        this.header = false;
        this.colspan = 1;
        this.rowSpan = 1;
        this.headers = new String[0];
    }

    public TableCellMarkup(String scope, String id) {
        this.scope = scope;
        this.id = id;
        this.header = true;
        this.colspan = 1;
        this.rowSpan = 1;
        this.headers = new String[0];
    }

    public TableCellMarkup(int colSpan, String scope, String id) {
        this.scope = scope;
        this.id = id;
        this.header = true;
        this.colspan = colSpan;
        this.rowSpan = 1;
        this.headers = new String[0];
    }

    public TableCellMarkup(String[] headers, String id) {
        this.scope = "";
        this.id = id;
        this.header = true;
        this.colspan = 1;
        this.rowSpan = 1;
        this.headers = headers;
    }

    public TableCellMarkup(String[] headers) {
        this.scope = "";
        this.id = "";
        this.header = false;
        this.colspan = 1;
        this.rowSpan = 1;
        this.headers = headers;
    }

    public boolean isHeader() {
        return header;
    }

    public String getId() {
        return id;
    }

    public String getScope() {
        return scope;
    }
    public String[] getHeaders() {
        return headers;
    }

    public int getRowSpan() {
        return rowSpan;
    }


    public int getColspan() {
        return colspan;
    }

}
