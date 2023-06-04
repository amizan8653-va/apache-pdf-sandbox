package com.wi.test.pojo;

import com.wi.test.enums.Font;

import java.awt.Color;

public class Cell extends Text {
    private final String align;
    private final boolean header;

    public Cell(String text, Font font, int fontSize, float width, String align, boolean header) {
        super(fontSize, width, text, Color.black, font);
        this.align = align;
        this.header = header;
    }

    public Cell(String text, Font font, Color textColor, int fontSize, float width, String align) {
        super(fontSize, width, text, textColor, font);
        this.align = align;
        this.header = false;
    }

    public String getAlign() {
        return align;
    }

    public boolean isHeader() {
        return header;
    }

}
