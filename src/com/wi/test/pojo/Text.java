package com.wi.test.pojo;

import java.awt.*;

public class Text {
    protected final int fontSize;
    protected final float width;

    protected final String text;
    protected final Color textColor;

    public Text(int fontSize, float width, String text, Color textColor) {
        this.fontSize = fontSize;
        this.width = width;
        this.text = text;
        this.textColor = textColor;
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


}
