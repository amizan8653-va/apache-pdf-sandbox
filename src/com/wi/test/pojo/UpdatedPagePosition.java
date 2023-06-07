package com.wi.test.pojo;

public class UpdatedPagePosition {
    float y;
    int pageIndex;

    public UpdatedPagePosition(float y, int pageIndex) {
        this.y = y;
        this.pageIndex = pageIndex;
    }

    public float getY() {
        return y;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public String toString() {
        return "UpdatedPagePosition{" +
            "y=" + y +
            ", pageIndex=" + pageIndex +
            '}';
    }
}
