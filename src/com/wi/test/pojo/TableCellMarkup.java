package com.wi.test.pojo;

public class TableCellMarkup {

    private final boolean header;
    private final String id;
    private final String scope;

    public TableCellMarkup() {
        this.scope = "";
        this.id = "";
        this.header = false;
    }

    public TableCellMarkup(String scope, String id) {
        this.scope = scope;
        this.id = id;
        this.header = true;
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

}
