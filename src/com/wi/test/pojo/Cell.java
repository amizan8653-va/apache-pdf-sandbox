package com.wi.test.pojo;

import com.wi.test.enums.Font;
import lombok.Getter;

import java.awt.Color;

@Getter
public class Cell extends Text {
    private final String align;
    private final boolean header;

    private final float width;

    public Cell(String text, Font font, int fontSize, float width, String align, boolean header) {
        super(fontSize, text, Color.black, font);
        this.width = width;
        this.align = align;
        this.header = header;
    }

    public Cell(String text, Font font, Color textColor, int fontSize, float width, String align) {
        super(fontSize, text, textColor, font);
        this.width = width;;
        this.align = align;
        this.header = false;
    }

}
