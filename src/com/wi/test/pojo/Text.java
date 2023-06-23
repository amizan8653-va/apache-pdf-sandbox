package com.wi.test.pojo;

import com.wi.test.enums.Font;
import lombok.Getter;

import java.awt.Color;

@Getter
public class Text {
    protected float fontSize;
    protected final String text;
    protected final Color textColor;
    protected final Font font;

    public void setFontSize(float fontSize){
        this.fontSize = fontSize;
    }

    public Text(float fontSize, String text, Color textColor, Font font) {
        this.fontSize = fontSize;
        this.text = text;
        this.textColor = textColor;
        this.font = font;
    }
}
