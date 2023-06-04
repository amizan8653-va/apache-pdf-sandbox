package com.wi.test.pojo;

import com.wi.test.enums.ListSubItemType;

import java.util.List;

public class ListItem {
    private final List<String> subItemsToPrint;
    private final List<ListSubItemType> subItemTypes;

    public ListItem(List<String> subItemsToPrint, List<ListSubItemType> subItemTypes) {
        this.subItemsToPrint = subItemsToPrint;
        this.subItemTypes = subItemTypes;
    }
}
