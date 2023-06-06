package com.wi.test.pojo;

public class PageMargins {
    float topMargin;
    float bottomMargin;
    float leftMargin;
    float rightMargin;

    public PageMargins(float topMargin, float bottomMargin, float leftMargin, float rightMargin) {
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
    }

    public float getTopMargin() {
        return topMargin;
    }

    public float getBottomMargin() {
        return bottomMargin;
    }

    public float getLeftMargin() {
        return leftMargin;
    }

    public float getRightMargin() {
        return rightMargin;
    }
}
