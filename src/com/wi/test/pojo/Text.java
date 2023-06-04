package com.wi.test.pojo;

import com.wi.test.enums.Font;

import java.awt.Color;

public class Text {
    protected final int fontSize;
    protected final float width;

    protected final String text;
    protected final Color textColor;

    protected final Font font;

    public Text(int fontSize, float width, String text, Color textColor, Font font) {
        this.fontSize = fontSize;
        this.width = width;
        this.text = text;
        this.textColor = textColor;
        this.font = font;
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

    public Font getFont() {
        return font;
    }
}
