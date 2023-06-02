package com.wi.test.pojo;

import java.awt.Color;

public class Cell {
    private String textVal = "";
    private final int fontSize;
    private final float width;
    private final String text;
    private final String align;
    private final Color textColor;
    private final TableCellMarkup cellMarkup;

    public Cell(String text, int fontSize, float width, String align, String textVal, TableCellMarkup cellMarkup) {
        this.text = text;
        this.fontSize = fontSize;
        this.textColor = Color.black;
        this.width = width;
        this.align = align;
        this.textVal = textVal;
        this.cellMarkup = cellMarkup;
    }

    public Cell(String text, int fontSize, float width, String align, TableCellMarkup cellMarkup) {
        this.text = text;
        this.fontSize = fontSize;
        this.textColor = Color.black;
        this.width = width;
        this.align = align;
        this.cellMarkup = cellMarkup;
    }

    public Cell(String text, Color textColor, int fontSize, float width, String align) {
        this.text = text;
        this.fontSize = fontSize;
        this.textColor = textColor;
        this.width = width;
        this.align = align;
        this.cellMarkup = new TableCellMarkup();
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

    public String getTextVal() {
        return textVal;
    }

    public void setTextVal(String textVal) {
        this.textVal = textVal;
    }

    public TableCellMarkup getCellMarkup() {
        return cellMarkup;
    }

}
