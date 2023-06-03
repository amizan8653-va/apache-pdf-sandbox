package com.wi.test.pojo;

public class TableCellMarkup {

    private final boolean header;
    private final String id;
    private final String scope;
    private final String[] headers;

    public TableCellMarkup() {
        this.scope = "";
        this.id = "";
        this.header = false;
        this.headers = new String[0];
    }

    public TableCellMarkup(String scope, String id) {
        this.scope = scope;
        this.id = id;
        this.header = true;
        this.headers = new String[0];
    }

    public TableCellMarkup(String[] headers, String id) {
        this.scope = "";
        this.id = id;
        this.header = true;
        this.headers = headers;
    }

    public TableCellMarkup(String[] headers) {
        this.scope = "";
        this.id = "";
        this.header = false;
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

}
