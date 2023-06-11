package com.wi.test.util;

import com.wi.test.constants.PDConstants;
import com.wi.test.enums.Font;
import com.wi.test.pojo.Cell;
import com.wi.test.pojo.DataTable;
import com.wi.test.pojo.NewPageRelatedVariables;
import com.wi.test.pojo.PageMargins;
import com.wi.test.pojo.Row;
import com.wi.test.pojo.Text;
import com.wi.test.pojo.UpdatedPagePosition;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.xml.XmpSerializer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import lombok.SneakyThrows;
public class CustomTaggedPdfBuilder {

    private final PDDocument pdf;
    private final ArrayList<PDPage> pages = new ArrayList<>();
    private final PDFont helveticaFont;
    private final PDFont helveticaBoldFont;
    private final COSArray nums = new COSArray();
    private final COSArray numDictionaries = new COSArray();
    private final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    public final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();

    private final PageMargins pageMargins;

    private int currentMCID = 0;
    private PDStructureElement rootElem;
    private COSDictionary currentMarkedContentDictionary;

    // variables for adding additional pages and text wrapping
    private final PDResources resources;
    private final COSArray cosArrayForAdditionalPages;
    private final COSArray boxArray;

    @SneakyThrows
    public CustomTaggedPdfBuilder(String title, PageMargins margins) {
        //Setup new document
        pdf = new PDDocument();
        pdf.setVersion(1.7f);
        pdf.getDocumentInformation().setTitle(title);

        this.pageMargins = margins;

        // setup the fonts and embed them
        resources = new PDResources();
        this.helveticaFont = PDType0Font.load(pdf,
            new PDTrueTypeFont(PDType1Font.HELVETICA.getCOSObject()).getTrueTypeFont(), true);
        resources.put(COSName.getPDFName("Helv"), helveticaFont);

        this.helveticaBoldFont = PDType0Font.load(pdf, new PDTrueTypeFont(PDType1Font.HELVETICA_BOLD.getCOSObject()).getTrueTypeFont(), true);
        resources.put(COSName.getPDFName("Helv-Bold"), helveticaBoldFont);

        cosArrayForAdditionalPages = new COSArray();
        boxArray = new COSArray();

        addXMPMetadata(title);
        setupDocumentCatalog();

        // setup page 1 & ability at add more pages.
        prePageOne();
        addPage();

    }

    @SneakyThrows
    public UpdatedPagePosition drawBulletList(List<Text> items, float x, float y, int pageIndex, PDStructureElement parent) {

        Font font =  items.get(0).getFont();
        PDFont pdFont = getPDFont(font);
        float fontSize =  items.get(0).getFontSize();

        final var prefix = "\u2022 ";
        final float prefixWidth = pdFont.getStringWidth(prefix) / 1000.0f * fontSize;
        final var lineLimit = PAGE_WIDTH - pageMargins.getLeftMargin() - pageMargins.getRightMargin() - prefixWidth;

        List<List<String>> wrappedListItems =  items.stream()
            .map(text -> computeWrappedLines(text, lineLimit))
            .collect(Collectors.toList());

        PDStructureElement pdfList = appendToTagTree(StandardStructureTypes.L, pages.get(pageIndex), parent);

        UpdatedPagePosition updatedPagePosition = null;
        float newY = y;
        for(int i = 0; i < items.size(); i++){

            PDStructureElement pdfListElement = appendToTagTree(StandardStructureTypes.LI, pages.get(pageIndex), pdfList);
            Text text = items.get(i);
            List<String> wrappedLines = wrappedListItems.get(i);
            updatedPagePosition = drawBulletListItem(prefix, text, wrappedLines, x, newY, pageIndex, pdfListElement, 5);
            newY = updatedPagePosition.getY();
            pageIndex = updatedPagePosition.getPageIndex();
        }
        return updatedPagePosition;
    }

    @SneakyThrows
    private NewPageRelatedVariables handlePageOverflow(PDPageContentStream oldContentStream, int pageIndex, PDStructureElement listItemParent, Text text, float x){
        oldContentStream.endText();

        //End the marked content and append it's P structure element to the containing P structure element.
        oldContentStream.endMarkedContent();
        appendToTagTree(pages.get(pageIndex), listItemParent);
        oldContentStream.close();


        addPage();


        //Set up the next marked content element with an MCID and create the containing P structure element.
        pageIndex += 1;
        PDPageContentStream newContentStream = new PDPageContentStream(
            pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        newContentStream.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));


        //Open up a stream to draw text at a given location.
        newContentStream.beginText();
        newContentStream.setFont(getPDFont(text.getFont()), text.getFontSize());
        float newInvertedYAxisOffset = PAGE_HEIGHT - this.pageMargins.getTopMargin();
        newContentStream.newLineAtOffset(x + this.pageMargins.getLeftMargin(), newInvertedYAxisOffset);
        newContentStream.setNonStrokingColor(text.getTextColor());

        return NewPageRelatedVariables.builder()
            .newContent(newContentStream)
            .newPageIndex(pageIndex)
            .newInvertedYAxisOffset(newInvertedYAxisOffset)
            .build();
    }

    @SneakyThrows
    private UpdatedPagePosition drawBulletListItem(String prefix, Text text, List<String> wrappedLines, float x, float y, int pageIndex, PDStructureElement listItemParent, float spaceBetweenListItems){

        // compute prefix width
        Font font =  text.getFont();
        PDFont pdFont = getPDFont(font);
        float fontSize =  text.getFontSize();
        final float prefixWidth = pdFont.getStringWidth(prefix) / 1000.0f * fontSize;

        float invertedYAxisOffset = PAGE_HEIGHT - y;

        //Set up the next marked content element with an MCID and create the containing P structure element.
        PDPageContentStream contents = new PDPageContentStream(
            pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

        //Open up a stream to draw text at a given location.
        contents.beginText();
        contents.setFont(getPDFont(text.getFont()), text.getFontSize());
        contents.newLineAtOffset(x + this.pageMargins.getLeftMargin(), invertedYAxisOffset);
        contents.setNonStrokingColor(text.getTextColor());
        PDStructureElement listTextTagElement = null;
        for (int i = 0; i < wrappedLines.size(); i++) {
            float newOffset = -text.getFontSize() - spaceBetweenListItems;
            invertedYAxisOffset += newOffset;
            if(invertedYAxisOffset <= this.pageMargins.getBottomMargin()) {
                var newPageVars = handlePageOverflow(contents, pageIndex, listItemParent, text, x);
                pageIndex = newPageVars.getNewPageIndex();
                invertedYAxisOffset = newPageVars.getNewInvertedYAxisOffset() - text.getFontSize() - spaceBetweenListItems;
                contents = newPageVars.getNewContent();
            }

            if(i == 0) {
                var bulletTagElement = appendToTagTree(StandardStructureTypes.LBL, pages.get(pageIndex), listItemParent);
                contents.showText(prefix);

                contents.newLineAtOffset(prefixWidth, 0);

                appendToTagTree(pages.get(pageIndex), bulletTagElement);

                // make the bullet point be tagged in just <LBL>, and the text right after separately in <LBODY>
                contents.endMarkedContent();

                // tag the list element's text body
                setNextMarkedContentDictionary();
                contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

                listTextTagElement = appendToTagTree(StandardStructureTypes.L_BODY, pages.get(pageIndex), listItemParent);
                String line = wrappedLines.get(i);
                contents.showText(line);
            } else {
                String line = wrappedLines.get(i);
                contents.showText(line);
            }

            contents.newLineAtOffset(0, newOffset);

        }
        contents.endText();
        appendToTagTree(pages.get(pageIndex), listTextTagElement);

        //End the marked content and append it's P structure element to the containing P structure element.
        contents.endMarkedContent();
        contents.close();

        return new UpdatedPagePosition(PAGE_HEIGHT - invertedYAxisOffset, pageIndex);

    }

    @SneakyThrows
    public UpdatedPagePosition drawTextElement(Text text, float x, float y, PDStructureElement parent,
                                              String structType, int pageIndex) {

        List<String> wrappedLines = computeWrappedLines(text, PAGE_WIDTH - pageMargins.getLeftMargin() - pageMargins.getRightMargin());

        y = y + this.pageMargins.getTopMargin();

        //Draws the given texts
        return drawSimpleText(text, wrappedLines, x, y, pageIndex, structType, parent, 5);
    }

    // Add text at a given location starting from the top-left corner.
    // this function is the core rendering logic shared by all.
    @SneakyThrows
    private UpdatedPagePosition drawSimpleText(Text text, List<String> wrappedLines, float x, float y, int pageIndex, String structType, PDStructureElement parent, float spaceBetweenLines){

        //Set up the next marked content element with an MCID and create the containing P structure element.
        PDPageContentStream contents = new PDPageContentStream(
            pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

        PDStructureElement currentElem = appendToTagTree(structType, pages.get(pageIndex), parent);


        //Open up a stream to draw text at a given location.
        contents.beginText();
        contents.setFont(getPDFont(text.getFont()), text.getFontSize());
        float invertedYAxisOffset = PAGE_HEIGHT - y;
        contents.newLineAtOffset(x + this.pageMargins.getLeftMargin(), invertedYAxisOffset);
        contents.setNonStrokingColor(text.getTextColor());
        for (int i = 0; i < wrappedLines.size(); i++) {
            float newOffset = -text.getFontSize() - spaceBetweenLines;
            invertedYAxisOffset += newOffset;
            if(invertedYAxisOffset <= this.pageMargins.getBottomMargin()) {
                var newPageVars = handlePageOverflow(contents, pageIndex, currentElem, text, x);
                pageIndex = newPageVars.getNewPageIndex();
                invertedYAxisOffset = newPageVars.getNewInvertedYAxisOffset();
                contents = newPageVars.getNewContent();
                currentElem = appendToTagTree(structType, pages.get(pageIndex), parent);
            }

            String line = wrappedLines.get(i);
            contents.showText(line);
            contents.newLineAtOffset(0, newOffset);
        }
        contents.endText();


        //End the marked content and append it's P structure element to the containing P structure element.
        contents.endMarkedContent();
        appendToTagTree(pages.get(pageIndex), currentElem);
        contents.close();

        return new UpdatedPagePosition(PAGE_HEIGHT - invertedYAxisOffset, pageIndex);

    }

    @SneakyThrows
    private List<String> computeWrappedLines(Text text, float lineLimit) {
        var font = getPDFont(text.getFont());
        var fontSize = text.getFontSize();
        List<List<String>> linesOfWords = Stream.of(text.getText().split("\n"))
            .map(line -> List.of(line.split(" ")))
            .collect(Collectors.toList());

        List<String> wrappedLines = new ArrayList<>();
        float spaceWidth  = font.getStringWidth(" ") / 1000.0f * fontSize;
        float currentLineWidth;
        int startingWordIndex;
        for (List<String> words : linesOfWords) {

            currentLineWidth = 0;
            startingWordIndex = 0;

            for (int i = 0; i < words.size(); i++) {
                float currentWordWidth = font.getStringWidth(words.get(i)) / 1000.0f * fontSize;
                currentLineWidth += currentWordWidth;

                if (currentLineWidth > lineLimit) {
                    // make a new line ending with the word before.
                    String line = String.join(" ", words.subList(startingWordIndex, i));
                    wrappedLines.add(line.trim() + " ");

                    // update starting word index to the current word. This word will be start of next line.
                    startingWordIndex = i;

                    // reset current line width back to width of the current word
                    currentLineWidth = currentWordWidth;
                } else {
                    // didn't hit a new line yet. Add a space to the count.
                    currentLineWidth += spaceWidth;
                }
            }
            // last line will have to be added.
            String lastLine = String.join(" ", words.subList(startingWordIndex, words.size()));
            wrappedLines.add(lastLine.trim() + " ");
        }

        return wrappedLines;
    }

    @SneakyThrows
    public UpdatedPagePosition drawTable(DataTable table, float x, float y, int pageIndex, PDStructureElement parent, float spaceBetweenLines) {

        COSDictionary attr = new COSDictionary();
        attr.setName(COSName.O, "Table");
        attr.setString(COSName.getPDFName("Summary"), table.getSummary());
        //Create a stream for drawing table's contents and append table structure element to the current form's structure element.
        PDStructureElement currentTable = appendToTagTree(StandardStructureTypes.TABLE, pages.get(pageIndex), parent);
        currentTable.getCOSObject().setItem(COSName.A, attr);
        currentTable.setAlternateDescription(table.getSummary());

        List<UpdatedPagePosition> aggregatedPositions = new ArrayList<>();

        int rowIndexStart = 0;

        //Go through each row and add a TR structure element to the table structure element.
        for (int i = 0; i < table.getRows().size(); i++) {

            //Go through each column and draw the cell and any cell's text with given alignment.
            PDStructureElement currentTR = appendToTagTree(StandardStructureTypes.TR, pages.get(pageIndex), currentTable);

            List<List<String>> wrappedLinesPerCell = table.getRows().get(i).getCells().stream()
                .map(cell -> computeWrappedLines(cell, cell.getWidth() * 0.9f))
                .collect(Collectors.toList());

            float maxFontSize = (float) table.getRows().get(i).getCells().stream()
                .mapToDouble(Text::getFontSize)
                .max()
                .orElseThrow();

            int maxNumberOfLines = wrappedLinesPerCell.stream()
                .mapToInt(List::size)
                .max()
                .orElseThrow();

            float newHeight = maxNumberOfLines * (maxFontSize + spaceBetweenLines);

            table.getRows().get(i).setHeight(newHeight);

            float cellY;
            if((y + (i + 1) * newHeight) >= (PAGE_HEIGHT - pageMargins.getBottomMargin() - pageMargins.getTopMargin())){
                addPage();
                pageIndex += 1;
                y = pageMargins.getTopMargin();
                rowIndexStart = i;
            }

            Row currentRow = table.getRows().get(i);

            cellY = y + table.getRowPosition(rowIndexStart, i);

            for(int j = 0; j < table.getRows().get(i).getCells().size(); j++) {

                Cell currentCell = table.getCell(i, j);
                float cellX = x + currentRow.getCellPosition(j);

                PDStructureElement cellStructureElement = addTableCellParentTag(currentCell, pageIndex, currentTR);
                UpdatedPagePosition updatedPagePosition = drawCellContents(pageIndex, wrappedLinesPerCell.get(j), currentRow, cellStructureElement, currentCell, cellX, cellY, spaceBetweenLines);
                aggregatedPositions.add(updatedPagePosition);
            }

        }

        final int returnPage = aggregatedPositions.stream()
            .mapToInt(UpdatedPagePosition::getPageIndex)
            .max()
            .orElseThrow();

        return aggregatedPositions.stream()
            .filter(position -> position.getPageIndex() == returnPage)
            .max(Comparator.comparing(UpdatedPagePosition::getY))
            .orElseThrow();

    }

    //Adds a SECT structure element as the structure tree root.
    public PDStructureElement addRoot(int pageIndex) {
        rootElem = new PDStructureElement(StandardStructureTypes.SECT, null);
        rootElem.setTitle("PDF Document");
        rootElem.setPage(pages.get(pageIndex));
        rootElem.setLanguage("EN-US");
        return rootElem;
    }

    //Save the pdf to disk and close the stream
    @SneakyThrows
    public void saveAndClose(String filePath) {
        addParentTree();
        pdf.save(filePath);
        pdf.close();
    }


    private PDStructureElement appendToTagTree(String structureType, PDPage currentPage, PDStructureElement parent){
        // Create a structure element and add it to the current section.
        // this is NOT marked content.
        PDStructureElement structureElement = new PDStructureElement(structureType, parent);
        structureElement.setPage(currentPage);
        parent.appendKid(structureElement);
        return structureElement;
    }

    private void appendToTagTree(PDPage currentPage, PDStructureElement parent){
        COSDictionary numDict = new COSDictionary();
        numDict.setInt(COSName.K, currentMCID - 1);
        numDict.setString(COSName.LANG, "EN-US");
        numDict.setItem(COSName.PG, currentPage.getCOSObject());

        PDMarkedContent markedContent = new PDMarkedContent(COSName.P, currentMarkedContentDictionary);
        parent.appendKid(markedContent);
        numDict.setItem(COSName.P, parent.getCOSObject());

        numDict.setName(COSName.S, COSName.P.getName());
        numDictionaries.add(numDict);
    }



    private PDStructureElement addTableCellParentTag(Cell cell, int pageIndex, PDStructureElement currentRow) {
        COSDictionary cellAttr = new COSDictionary();
        cellAttr.setName(COSName.O, "Table");
        String structureType = cell.isHeader() ? StandardStructureTypes.TH : StandardStructureTypes.TD;
        PDStructureElement cellElement = appendToTagTree(structureType, pages.get(pageIndex), currentRow);
        cellElement.getCOSObject().setItem(COSName.A, cellAttr);
        return cellElement;
    }

    private UpdatedPagePosition drawCellContents(int pageIndex, List<String> wrappedLines, Row currentRow, PDStructureElement cellStructureElement, Cell currentCell, float cellX, float cellY, float spaceBetweenLines) {
        //Draw the cell's text with a given alignment, and tag it.
        return switch (currentCell.getAlign()) {
            case PDConstants.CENTER_ALIGN -> drawSimpleText(currentCell, wrappedLines,
                cellX + currentCell.getWidth() / 2.0f - currentCell.getFontSize() / 3.75f * currentCell.getText().length(),
                cellY + currentRow.getHeight() / 2.0f + currentCell.getFontSize() / 4.0f,
                pageIndex,
                StandardStructureTypes.SPAN,
                cellStructureElement,
                spaceBetweenLines);
            case PDConstants.TOP_ALIGN -> drawSimpleText(currentCell, wrappedLines,
                cellX + 5,
                cellY + currentCell.getFontSize() / 4.0f + 5,
                pageIndex,
                StandardStructureTypes.SPAN,
                cellStructureElement,
                spaceBetweenLines);
            case PDConstants.LEFT_ALIGN -> drawSimpleText(currentCell, wrappedLines,
                cellX + 5,
                cellY + currentRow.getHeight() / 2 + currentCell.getFontSize() / 4.0f,
                pageIndex,
                StandardStructureTypes.SPAN,
                cellStructureElement,
                spaceBetweenLines);
            default -> throw new RuntimeException("invalid text justification used.");
        };
    }

    private PDFont getPDFont(Font font) {
        return switch(font) {
            case HELVETICA -> this.helveticaFont;
            case HELVETICA_BOLD -> this.helveticaBoldFont;
        };
    }

    //Assign an id for the next marked content element.
    private void setNextMarkedContentDictionary() {
        currentMarkedContentDictionary = new COSDictionary();
        currentMarkedContentDictionary.setInt(COSName.MCID, currentMCID);
        currentMCID++;
    }

    @SneakyThrows
    private void addXMPMetadata(String title) {
        //Add UA XMP metadata based on specs at https://taggedpdf.com/508-pdf-help-center/pdfua-identifier-missing/
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();
        xmp.createAndAddDublinCoreSchema();
        xmp.getDublinCoreSchema().setTitle(title);
        xmp.getDublinCoreSchema().setDescription(title);
        xmp.createAndAddPDFAExtensionSchemaWithDefaultNS();
        xmp.getPDFExtensionSchema().addNamespace("http://www.aiim.org/pdfa/ns/schema#", "pdfaSchema");
        xmp.getPDFExtensionSchema().addNamespace("http://www.aiim.org/pdfa/ns/property#", "pdfaProperty");
        xmp.getPDFExtensionSchema().addNamespace("http://www.aiim.org/pdfua/ns/id/", "pdfuaid");
        XMPSchema uaSchema = new XMPSchema(XMPMetadata.createXMPMetadata(),
                "pdfaSchema", "pdfaSchema", "pdfaSchema");
        uaSchema.setTextPropertyValue("schema", "PDF/UA Universal Accessibility Schema");
        uaSchema.setTextPropertyValue("namespaceURI", "http://www.aiim.org/pdfua/ns/id/");
        uaSchema.setTextPropertyValue("prefix", "pdfuaid");
        XMPSchema uaProp = new XMPSchema(XMPMetadata.createXMPMetadata(),
                "pdfaProperty", "pdfaProperty", "pdfaProperty");
        uaProp.setTextPropertyValue("name", "part");
        uaProp.setTextPropertyValue("valueType", "Integer");
        uaProp.setTextPropertyValue("category", "internal");
        uaProp.setTextPropertyValue("description", "Indicates, which part of ISO 14289 standard is followed");
        uaSchema.addUnqualifiedSequenceValue("property", uaProp);
        xmp.getPDFExtensionSchema().addBagValue("schemas", uaSchema);
        xmp.getPDFExtensionSchema().setPrefix("pdfuaid");
        xmp.getPDFExtensionSchema().setTextPropertyValue("part", "1");
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp, baos, true);
        PDMetadata metadata = new PDMetadata(pdf);
        metadata.importXMPMetadata(baos.toByteArray());
        pdf.getDocumentCatalog().setMetadata(metadata);
    }

    private void setupDocumentCatalog() {
        //Adjust other document metadata
        PDDocumentCatalog documentCatalog = pdf.getDocumentCatalog();
        documentCatalog.setLanguage("English");
        documentCatalog.setViewerPreferences(new PDViewerPreferences(new COSDictionary()));
        documentCatalog.getViewerPreferences().setDisplayDocTitle(true);
        documentCatalog.getCOSObject().setString(COSName.LANG, "EN-US");
        documentCatalog.getCOSObject().setName(COSName.PAGE_LAYOUT, "OneColumn");
        PDStructureTreeRoot structureTreeRoot = new PDStructureTreeRoot();
        documentCatalog.setStructureTreeRoot(structureTreeRoot);
        PDMarkInfo markInfo = new PDMarkInfo();
        markInfo.setMarked(true);
        documentCatalog.setMarkInfo(markInfo);
    }

    private void prePageOne(){
        //Create document initial pages
        cosArrayForAdditionalPages.add(COSName.getPDFName("PDF"));
        cosArrayForAdditionalPages.add(COSName.getPDFName("Text"));
        boxArray.add(new COSFloat(0.0f));
        boxArray.add(new COSFloat(0.0f));
        boxArray.add(new COSFloat(612.0f));
        boxArray.add(new COSFloat(792.0f));
    }
    private void addPage(){
        PDPage page = new PDPage(PDRectangle.LETTER);
        page.getCOSObject().setItem(COSName.getPDFName("Tabs"), COSName.S);
        page.setResources(resources);
        page.getResources().getCOSObject().setItem(COSName.PROC_SET, cosArrayForAdditionalPages);
        page.getCOSObject().setItem(COSName.CROP_BOX, boxArray);
        page.getCOSObject().setItem(COSName.ROTATE, COSInteger.get(0));
        page.getCOSObject().setItem(COSName.STRUCT_PARENTS, COSInteger.get(0));
        pages.add(page);
        pdf.addPage(pages.get(pages.size() - 1));
    }

    //Adds the parent tree to root struct element to identify tagged content
    private void addParentTree() {
        COSDictionary dict = new COSDictionary();
        nums.add(numDictionaries);
        dict.setItem(COSName.NUMS, nums);
        PDNumberTreeNode numberTreeNode = new PDNumberTreeNode(dict, dict.getClass());
        int currentStructParent = 1;
        pdf.getDocumentCatalog().getStructureTreeRoot().setParentTreeNextKey(currentStructParent);
        pdf.getDocumentCatalog().getStructureTreeRoot().setParentTree(numberTreeNode);
        pdf.getDocumentCatalog().getStructureTreeRoot().appendKid(rootElem);
    }
}
