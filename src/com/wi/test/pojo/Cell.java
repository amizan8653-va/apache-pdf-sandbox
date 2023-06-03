package com.wi.test.pojo;

import java.awt.Color;

public class Cell {
    private final int fontSize;
    private final float width;
    private final String text;
    private final String align;
    private final Color textColor;
    private final boolean header;

    public Cell(String text, int fontSize, float width, String align, boolean header) {
        this.text = text;
        this.fontSize = fontSize;
        this.textColor = Color.black;
        this.width = width;
        this.align = align;
        this.header = header;
    }

    public Cell(String text, Color textColor, int fontSize, float width, String align) {
        this.text = text;
        this.fontSize = fontSize;
        this.textColor = textColor;
        this.width = width;
        this.align = align;
        this.header = false;
    }

    public String getAlign() {
        return align;
    }

    public String getText() {
        return text;
    }


    public Color getTextColor() {
        return textColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public float getWidth() {
        return width;
    }


    public boolean isHeader() {
        return header;
    }

}
