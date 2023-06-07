package com.wi.test.util;

import com.wi.test.constants.PDConstants;
import com.wi.test.enums.Font;
import com.wi.test.pojo.Cell;
import com.wi.test.pojo.DataTable;
import com.wi.test.pojo.PageMargins;
import com.wi.test.pojo.Row;
import com.wi.test.pojo.Text;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.*;
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
import org.apache.xmpbox.schema.XmpSchemaException;
import org.apache.xmpbox.xml.XmpSerializer;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CustomTaggedPdfBuilder {

    private final PDDocument pdf;
    private final ArrayList<PDPage> pages = new ArrayList<>();
    private final PDFont helveticaFont;
    private final PDFont helveticaBoldFont;
    private final COSArray nums = new COSArray();
    private final COSArray numDictionaries = new COSArray();
    private float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    public float PAGE_WIDTH = PDRectangle.LETTER.getWidth();

    private final PageMargins pageMargins;

    private int currentMCID = 0;
    private PDStructureElement rootElem;
    private COSDictionary currentMarkedContentDictionary;

    // variables for adding additional pages and text wrapping
    private final PDResources resources;
    private final COSArray cosArrayForAdditionalPages;
    private final COSArray boxArray;

    private final float wrappedTextMultiplier;

    public CustomTaggedPdfBuilder(String title, PageMargins margins, float wrappedTextMultiplier) throws IOException, TransformerException, XmpSchemaException {
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
        postPageOne();

        this.wrappedTextMultiplier = wrappedTextMultiplier;

    }

//    public void drawBulletList(List<String> items, float x, float y, PDStructureElement parent, int pageIndex) throws IOException{
//        PDStructureElement listContainer = appendToTagTree(StandardStructureTypes.L, pages.get(pageIndex), parent);
//        for(String item: items){
//            PDStructureElement listElementContainer = appendToTagTree(StandardStructureTypes.LI, pages.get(pageIndex), listContainer);
//            PDStructureElement ListElementBulletElementContainer = appendToTagTree(StandardStructureTypes.LBL, pages.get(pageIndex), listElementContainer);
//            appendToTagTree(pages.get(pageIndex), ListElementBulletElementContainer);
//
//            PDPageContentStream contents = new PDPageContentStream(
//                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
//            setNextMarkedContentDictionary();
//            contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));
//
//            //Draws the given text centered within the current table cell.
//            drawSimpleText(Character.toString('\u2022'), x + 5, y + height + text.getFontSize(), contents);
//
//            //End the marked content and append it's P structure element to the containing P structure element.
//            contents.endMarkedContent();
//            appendToTagTree(pages.get(pageIndex), currentElem);
//            contents.close();
//
//            // U+2022 = bullet point
//            PDStructureElement ListElementParagraphContainer = appendToTagTree(StandardStructureTypes.L_BODY, pages.get(pageIndex), listElementContainer);
//        }
//    }

    public PDStructureElement drawTextElement(Text text, float x, float y, float yOffset, PDStructureElement parent,
                                              String structType, int pageIndex) throws Exception {

        List<String> wrappedLines = computeWrappedLines(text, PAGE_WIDTH - pageMargins.getLeftMargin() - pageMargins.getRightMargin());
        PDStructureElement currentElem = appendToTagTree(structType, pages.get(pageIndex), parent);


        //Set up the next marked content element with an MCID and create the containing P structure element.
        PDPageContentStream contents = new PDPageContentStream(
                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

        //Draws the given text centered within the current table cell.
        drawSimpleText(text, wrappedLines,x, y + yOffset + text.getFontSize(), contents);

        //End the marked content and append it's P structure element to the containing P structure element.
        contents.endMarkedContent();
        appendToTagTree(pages.get(pageIndex), currentElem);
        contents.close();
        return currentElem;
    }

    private List<String> computeWrappedLines(Text text, float lineLimit) throws Exception {
        var font = getPDFont(text.getFont());
        var fontSize = text.getFontSize();
        List<String> words = List.of(text.getText().split(" "));
        List<String> wrappedLines = new ArrayList<>();
        float spaceWidth  = font.getStringWidth(" ") / 1000.0f * fontSize;
        float currentLineWidth = 0;
        int startingWordIndex = 0;
        for (int i = 0; i < words.size(); i++) {
            float currentWordWidth = font.getStringWidth(words.get(i)) / 1000.0f * fontSize;
            currentLineWidth  += currentWordWidth;

            if(currentLineWidth >  lineLimit){
                // make a new line ending with the word before.
                wrappedLines.add(String.join(" ", words.subList(startingWordIndex, i)));

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
        wrappedLines.add(lastLine);

        return wrappedLines;
    }

    //Given a DataTable will draw each cell and any given text.
    public void drawTable(DataTable table, float x, float y, int pageIndex, PDStructureElement parent) throws Exception {

        COSDictionary attr = new COSDictionary();
        attr.setName(COSName.O, "Table");
        attr.setString(COSName.getPDFName("Summary"), table.getSummary());
        //Create a stream for drawing table's contents and append table structure element to the current form's structure element.
        PDStructureElement currentTable = appendToTagTree(StandardStructureTypes.TABLE, pages.get(pageIndex), parent);
        currentTable.getCOSObject().setItem(COSName.A, attr);
        currentTable.setAlternateDescription(table.getSummary());

        //Go through each row and add a TR structure element to the table structure element.
        for (int i = 0; i < table.getRows().size(); i++) {

            //Go through each column and draw the cell and any cell's text with given alignment.
            PDStructureElement currentTR = appendToTagTree(StandardStructureTypes.TR, pages.get(pageIndex), currentTable);
            Row currentRow = table.getRows().get(i);

            for(int j = 0; j < table.getRows().get(i).getCells().size(); j++) {

                Cell currentCell = table.getCell(i, j);
                float cellX = x + currentRow.getCellPosition(j);
                float cellY = y + table.getRowPosition(i);
                PDStructureElement cellStructureElement = addTableCellParentTag(currentCell, pageIndex, currentTR);
                drawCellContents(pageIndex, currentRow, cellStructureElement, currentCell, cellX, cellY);
            }

        }

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
    public void saveAndClose(String filePath) throws IOException {
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

    private void drawCellContents(int pageIndex, Row currentRow, PDStructureElement cellStructureElement, Cell currentCell, float cellX, float cellY) throws Exception {
        setNextMarkedContentDictionary();
        //Draw the cell's text with a given alignment, and tag it.
        PDPageContentStream contents = new PDPageContentStream(
                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));
        List<String> wrappedLines = computeWrappedLines(currentCell, currentCell.getWidth());
        switch (currentCell.getAlign()) {
            case PDConstants.CENTER_ALIGN -> drawSimpleText(currentCell, wrappedLines,
                cellX + currentCell.getWidth() / 2.0f - currentCell.getFontSize() / 3.75f * currentCell.getText().length(),
                cellY + currentRow.getHeight() / 2.0f + currentCell.getFontSize() / 4.0f,
                contents);
            case PDConstants.TOP_ALIGN -> drawSimpleText(currentCell, wrappedLines,
                cellX + 5,
                cellY + currentCell.getFontSize() / 4.0f + 5,
                contents);
            case PDConstants.LEFT_ALIGN -> drawSimpleText(currentCell, wrappedLines,
                cellX + 5,
                cellY + currentRow.getHeight() / 2 + currentCell.getFontSize() / 4.0f,
                contents);
        }

        //End the marked content and append it's P structure element to the containing TD structure element.
        contents.endMarkedContent();
        appendToTagTree(pages.get(pageIndex), cellStructureElement);
        contents.close();
    }

    //Add text at a given location starting from the top-left corner.
    private void drawSimpleText(Text text, List<String> wrappedLines, float x, float y, PDPageContentStream contents) throws IOException {
        //Open up a stream to draw text at a given location.
        contents.beginText();
        contents.setFont(getPDFont(text.getFont()), text.getFontSize());
        contents.newLineAtOffset(x + this.pageMargins.getLeftMargin(), PAGE_HEIGHT - y - this.pageMargins.getTopMargin());
        contents.setNonStrokingColor(text.getTextColor());
        for (String s: wrappedLines) {
            contents.showText(s);
            contents.newLineAtOffset(0, -(text.getFontSize() * this.wrappedTextMultiplier));
        }
        contents.endText();
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

    private void addXMPMetadata(String title) throws TransformerException, IOException {
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

    private void postPageOne(){
        nums.add(COSInteger.get(0));
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
