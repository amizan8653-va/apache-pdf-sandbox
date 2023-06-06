package com.wi.test.pojo;

import com.wi.test.enums.Font;

import java.awt.Color;

public class Text {
    protected final float fontSize;

    protected final String text;
    protected final Color textColor;
    protected final Font font;

    public Text(float fontSize, String text, Color textColor, Font font) {
        this.fontSize = fontSize;
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

    public float getFontSize() {
        return fontSize;
    }

    public Font getFont() {
        return font;
    }
}
