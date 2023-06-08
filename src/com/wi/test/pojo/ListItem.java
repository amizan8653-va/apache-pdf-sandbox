package com.wi.test.pojo;

import com.wi.test.enums.ListSubItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ListItem {
    private final List<String> subItemsToPrint;
    private final List<ListSubItemType> subItemTypes;
}
