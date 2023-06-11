package com.wi.test.pojo;

import lombok.Builder;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

@Builder
@Getter
public class NewPageRelatedVariables{
    PDPageContentStream newContent;
    float newInvertedYAxisOffset;
    int newPageIndex;
}