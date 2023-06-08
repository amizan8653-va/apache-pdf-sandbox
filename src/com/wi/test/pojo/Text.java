package com.wi.test.pojo;

import com.wi.test.enums.Font;
import lombok.Getter;

import java.awt.Color;

@Getter
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
}
