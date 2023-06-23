package com.wi.test.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdatedPagePosition {
    private final float y;
    private final int pageIndex;

    @Override
    public String toString() {
        return "UpdatedPagePosition{" +
            "y=" + y +
            ", pageIndex=" + pageIndex +
            '}';
    }
}
