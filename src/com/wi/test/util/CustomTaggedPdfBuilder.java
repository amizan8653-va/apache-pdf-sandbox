package com.wi.test.util;

import com.wi.test.constants.PDConstants;
import com.wi.test.enums.Font;
import com.wi.test.enums.TableHeaderType;
import com.wi.test.pojo.Cell;
import com.wi.test.pojo.DataTable;
import com.wi.test.pojo.NewPageRelatedVariables;
import com.wi.test.pojo.PageMargins;
import com.wi.test.pojo.Row;
import com.wi.test.pojo.Text;
import com.wi.test.pojo.UpdatedPagePosition;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDObjectReference;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDTableAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.xml.XmpSerializer;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import lombok.SneakyThrows;

import static org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink.HIGHLIGHT_MODE_NONE;

public class CustomTaggedPdfBuilder {

    // create accessible letter PDFs
    // note: about link tags: https://accessible-digital-documents.com/blog/accessible-pdf-links/
    // note: tags accepted for PDF/UA-1: https://www.pubcom.com/blog/2020_05-02_tags/pdf-ua-tags.shtml

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

    // will match urls such as "www.healthcare.gov" or "https://www.va.gov/health-care/about-affordable-care-act"

    private final String URL_REGEX = "(http|www)[^\\s]*[a-zA-Z0-9]";
    private final String PHONE_NUMBER_REGEX = "(1-)?\\d{3}-\\d{3}-\\d{4}";
    private final String URL_OR_PHONE_NUMBER_REGEX = String.format("(%s|%s)", URL_REGEX, PHONE_NUMBER_REGEX);
    private final Pattern URL_OR_PHONE_NUMBER_PATTERN = Pattern.compile(URL_OR_PHONE_NUMBER_REGEX);

    private final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);

    private static final String WATERMARK_PATH_FILENAME = "watermark.jpg";

    private static final String CARD_WATERMARK_FILENAME = "card-watermark.jpg";

    private static final String VA_LOGO_FILENAME = "va_seal.jpg";

    private byte[] watermarkBytes;

    /** The proof of service card water mark bytes. */
    private byte[] cardWatermarkBytes;

    private byte [] vaSealBytes;

    public void loadExternalImageBytes() {
        watermarkBytes = CustomTaggedPdfBuilder.readBinaryFile(WATERMARK_PATH_FILENAME);
        cardWatermarkBytes = CustomTaggedPdfBuilder.readBinaryFile(CARD_WATERMARK_FILENAME);
        vaSealBytes = CustomTaggedPdfBuilder.readBinaryFile(VA_LOGO_FILENAME);
    }


    @SneakyThrows
    public CustomTaggedPdfBuilder(String title, PageMargins margins) {
        loadExternalImageBytes();

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

        addRoot(0);

        addImagesToPage();

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
    private NewPageRelatedVariables handlePageOverflow(PDPageContentStream oldContentStream, int pageIndex, PDStructureElement parentElement, Text text, float x){
        oldContentStream.endText();

        //End the marked content and append it's P structure element to the containing P structure element.
        oldContentStream.endMarkedContent();
        appendToTagTree(pages.get(pageIndex), parentElement);
        oldContentStream.close();


        addPage();
        addImagesToPage();


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

        float lineOffset = -text.getFontSize() - spaceBetweenListItems;

        // do not try to break a single list item across multiple pages: tagging gets screwed up
        // instead, compute if all lines fit as a single unit.
        // If so, add the unit to the current page. If not, add a new page before adding the unit.
        // This assumes that a single bullet item on a list will *not* be longer than an entire page.
        if( (invertedYAxisOffset + lineOffset * wrappedLines.size()) <= this.pageMargins.getBottomMargin()) {
            var newPageVars = handlePageOverflow(contents, pageIndex, listItemParent, text, x);
            pageIndex = newPageVars.getNewPageIndex();
            invertedYAxisOffset = newPageVars.getNewInvertedYAxisOffset();
            contents = newPageVars.getNewContent();
        }


        for (int i = 0; i < wrappedLines.size(); i++) {
            invertedYAxisOffset += lineOffset;
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

            }

            String line = wrappedLines.get(i);
            contents.showText(line);
            contents.newLineAtOffset(0, lineOffset);

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

        PDFont pdFont = getPDFont(text.getFont());
        float fontSize =  text.getFontSize();

        //Open up a stream to draw text at a given location.
        contents.beginText();
        contents.setFont(getPDFont(text.getFont()), text.getFontSize());
        float invertedYAxisOffset = PAGE_HEIGHT - y;
        contents.newLineAtOffset(x + this.pageMargins.getLeftMargin(), invertedYAxisOffset);
        contents.setNonStrokingColor(text.getTextColor());
        boolean lastLineIsLink = false;
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

            Matcher matcher = URL_OR_PHONE_NUMBER_PATTERN.matcher(line);
            if(matcher.find()) {
                //get the MatchResult Object
                MatchResult regexMatch = matcher.toMatchResult();

                String beforeLinkText = line.substring(0, regexMatch.start());
                float beforeLinkTextWidth = pdFont.getStringWidth(beforeLinkText) / 1000.0f * fontSize;
                String linkText = matcher.group();
                float linkTextWidth = pdFont.getStringWidth(linkText) / 1000.0f * fontSize;
                String afterLinkText = line.substring(regexMatch.end());

                // prefix before the link
                contents.setNonStrokingColor(text.getTextColor());
                contents.showText(beforeLinkText);
                appendToTagTree(pages.get(pageIndex), currentElem);

                // segment tags
                contents.endMarkedContent();
                setNextMarkedContentDictionary();
                contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

                // actual link
                var linkElem = appendToTagTree(StandardStructureTypes.LINK, pages.get(pageIndex), currentElem);
                contents.setNonStrokingColor(Color.blue);
                contents.newLineAtOffset(beforeLinkTextWidth, 0);
                contents.showText(linkText);

                // link annotation creation and tagging
                appendToLinkAnnotationToLinkTag(
                        linkText,
                        linkElem,
                        x + this.pageMargins.getLeftMargin() + beforeLinkTextWidth,
                        invertedYAxisOffset,
                        linkTextWidth,
                        text.getFontSize());


                // segment tags
                appendToTagTree(pages.get(pageIndex), linkElem);
                contents.endMarkedContent();
                setNextMarkedContentDictionary();
                contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

                // postfix after the link
                contents.setNonStrokingColor(text.getTextColor());
                contents.newLineAtOffset(linkTextWidth, 0);
                contents.showText(afterLinkText);
                appendToTagTree(pages.get(pageIndex), currentElem);

                // segment text
                contents.endMarkedContent();
                setNextMarkedContentDictionary();
                contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

                contents.newLineAtOffset(-(beforeLinkTextWidth + linkTextWidth), newOffset);
                lastLineIsLink = true;
            } else {
                contents.showText(line);
                contents.newLineAtOffset(0, newOffset);
                lastLineIsLink = false;
            }

        }
        contents.endText();


        //End the marked content and append it's P structure element to the containing P structure element.
        if(!lastLineIsLink) {
            contents.endMarkedContent();
            appendToTagTree(pages.get(pageIndex), currentElem);
        }

        contents.close();

        return new UpdatedPagePosition(PAGE_HEIGHT - invertedYAxisOffset, pageIndex);

    }

    @SneakyThrows
    private void appendToLinkAnnotationToLinkTag(String hyperLinkOrPhoneNumber, PDStructureElement linkElem, float x, float y, float width, float height) {
        // the question & this answer https://stackoverflow.com/a/21163795/4832515 were the basis for this code
        PDAnnotationLink linkAnnotation = new PDAnnotationLink();
        linkAnnotation.setHighlightMode(HIGHLIGHT_MODE_NONE);
        PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
        borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
        linkAnnotation.setBorderStyle(borderULine);
        float [] blue =  {0.0f, 0.0f, 1.0f};
        linkAnnotation.setColor(new PDColor(blue, PDDeviceRGB.INSTANCE));

        Matcher matcher = PHONE_NUMBER_PATTERN.matcher(hyperLinkOrPhoneNumber);
        var action = new PDActionURI();
        if(matcher.find()) {
            // this is a phone number, stick 'tel:' as a prefix to denote that.
            hyperLinkOrPhoneNumber = "tel:" + hyperLinkOrPhoneNumber;
        }
        action.setURI(hyperLinkOrPhoneNumber);
        linkAnnotation.setAction(action);

        // set position of annotation on page.
        PDRectangle position = new PDRectangle();
        position.setLowerLeftX(x);
        position.setLowerLeftY(y + height);
        position.setUpperRightX(x + width);
        // todo: make sure this 2.5x multiplier works with multiple font sizes. Maybe this only works with size you
        //       are testing with.
        position.setUpperRightY(y + 2.5f * height);
        linkAnnotation.setRectangle(position);

        // this line is evil... it will add a link to your page but it will be an unmarked annotation failing commonlook
        // rather than using this to add a link to your page, you should go and create an object reference that
        // wraps around your annotation and add that object reference to the <LINK> element.
        // pdf.getPage(pageIndex).getAnnotations().add(linkAnnotation);

        PDObjectReference objectReference = new PDObjectReference();
        objectReference.setReferencedObject(linkAnnotation);
        linkAnnotation.setContents(hyperLinkOrPhoneNumber);
        linkElem.appendKid(objectReference);
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
                addImagesToPage();
            }

            Row currentRow = table.getRows().get(i);

            cellY = y + table.getRowPosition(rowIndexStart, i);

            for(int j = 0; j < table.getRows().get(i).getCells().size(); j++) {

                Cell currentCell = table.getCell(i, j);
                float cellX = x + currentRow.getCellPosition(j);

                PDStructureElement cellStructureElement = addTableCellParentTag(currentCell, pageIndex, currentTR, table.getTableHeaderType());
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
    public void addRoot(int pageIndex) {
        rootElem = new PDStructureElement(StandardStructureTypes.SECT, null);
        rootElem.setTitle("PDF Document");
        rootElem.setPage(pages.get(pageIndex));
        rootElem.setLanguage("EN-US");
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

    @SneakyThrows
    private void appendArtifactToPage(PDPageContentStream contentStream, int pageIndex){
        //numDict for parent tree
        COSDictionary numDict = new COSDictionary();
        numDict.setInt(COSName.K, currentMCID - 1);
        numDict.setString(COSName.LANG, "EN-US");
        numDict.setItem(COSName.PG, pdf.getPage(pageIndex).getCOSObject());

        numDict.setName(COSName.S, COSName.ARTIFACT.getName());
        numDictionaries.add(numDict);

        contentStream.endMarkedContent();
        contentStream.close();

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


    private PDStructureElement addTableCellParentTag(Cell cell, int pageIndex, PDStructureElement currentRow, TableHeaderType tableHeaderType) {
        COSDictionary cellAttr = new COSDictionary();
        cellAttr.setName(COSName.O, "Table");
        String structureType = cell.isHeader() ? StandardStructureTypes.TH : StandardStructureTypes.TD;
        if(cell.isHeader()){
            if(tableHeaderType == TableHeaderType.ROW_HEADERS){
                cellAttr.setName(COSName.getPDFName("Scope"), PDTableAttributeObject.SCOPE_COLUMN);
            } else {
                // for some reason commonlook will complain about this.
                // it isn't happy about table cells only having row headers & not any column headers.
                cellAttr.setName(COSName.getPDFName("Scope"), PDTableAttributeObject.SCOPE_ROW);
            }
        }
        cellAttr.setInt(COSName.getPDFName("ColSpan"), 1);
        cellAttr.setInt(COSName.getPDFName("RowSpan"), 1);
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

    private void addImagesToPage() {
        PDPageContentStream contentStream = drawStandardWatermark(pages.size() - 1);
        appendArtifactToPage(contentStream, pages.size() - 1);
    }

    //Adds the parent tree to root struct element to identify tagged content
    private void addParentTree() {
        COSDictionary dict = new COSDictionary();
        nums.add(numDictionaries);
        dict.setItem(COSName.NUMS, nums);
        PDNumberTreeNode numberTreeNode = new PDNumberTreeNode(dict, dict.getClass());
        pdf.getDocumentCatalog().getStructureTreeRoot().setParentTree(numberTreeNode);
        pdf.getDocumentCatalog().getStructureTreeRoot().appendKid(rootElem);
    }

    @SneakyThrows
    public static byte[] readBinaryFile(final String filePath) {
        return CustomTaggedPdfBuilder.class
                .getClassLoader()
                .getResource(filePath)
                .openStream()
                .readAllBytes();
    }

    private PDPageContentStream drawStandardWatermark(int pageIndex){
        var marginTop = 4f / 11.7f * pdf.getPage(pageIndex).getMediaBox().getHeight();
        var marginLeft = 1.75f / 8.25f * pdf.getPage(pageIndex).getMediaBox().getWidth();
        var width = 360;
        var height = 354;
        return drawImage(watermarkBytes, "Watermark", pageIndex, marginTop, marginLeft, width, height);
    }

    @SneakyThrows
    private PDPageContentStream drawImage(byte [] imageBytes, String imageName, int pageIndex, float marginTop, float marginLeft, int width, int height) {
        PDImageXObject pdImageXObject =
                PDImageXObject.createFromByteArray(pdf, imageBytes, imageName);
        PDPage page = pdf.getPage(pageIndex);
        PDPageContentStream cos =
                new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true);
        setNextMarkedContentDictionary();
        cos.beginMarkedContent(COSName.ARTIFACT, PDPropertyList.create(currentMarkedContentDictionary));
        cos.drawImage(pdImageXObject, marginLeft, marginTop, width, height);
        return cos;
    }

    public PDStructureElement getRoot() {
        return rootElem;
    }
}
