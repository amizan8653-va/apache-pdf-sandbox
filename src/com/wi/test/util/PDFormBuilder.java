package com.wi.test.util;

import com.wi.test.constants.PDConstants;
import com.wi.test.pojo.Cell;
import com.wi.test.pojo.DataTable;
import com.wi.test.pojo.Row;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.*;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDObjectReference;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDArtifactMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XmpSchemaException;
import org.apache.xmpbox.xml.XmpSerializer;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class PDFormBuilder {

    private PDDocument pdf = null;
    private PDAcroForm acroForm = null;
    private ArrayList<PDPage> pages = new ArrayList<>();
    private ArrayList<COSDictionary> annotDicts = new ArrayList<>();
    private ArrayList<PDObjectReference> annotationRefs = new ArrayList<>();
    private ArrayList<PDField> fields = new ArrayList<>();
    private ArrayList<PDAnnotationWidget> widgets = new ArrayList<>();
    private PDFont defaultFont = null;
    private PDStructureElement rootElem = null;
    private PDStructureElement currentElem = null;
    private COSDictionary currentMarkedContentDictionary;
    private COSArray nums = new COSArray();
    private COSArray numDictionaries = new COSArray();
    private int currentMCID = 0;
    private int currentStructParent = 1;
    private final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    public final float PAGE_WIDTH = PDRectangle.A4.getWidth();

    public PDFormBuilder(int initPages, String title) throws IOException, TransformerException, XmpSchemaException {

        //Setup new document
        pdf = new PDDocument();
        acroForm = new PDAcroForm(pdf);
        pdf.getDocumentInformation().setTitle(title);
        PDResources resources = setupAcroForm();
        addXMPMetadata(title);
        setupDocumentCatalog();
        initiatePages(initPages, resources);

    }

    public PDStructureElement drawElement(Cell textCell, float x, float y, float height, PDStructureElement parent,
                                            String structType, int pageIndex) throws IOException {

        //Set up the next marked content element with an MCID and create the containing H1 structure element.
        PDPageContentStream contents = new PDPageContentStream(
                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        currentElem = addContentToParent(null, structType, pages.get(pageIndex), parent);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.ARTIFACT, PDPropertyList.create(currentMarkedContentDictionary));
        // originally a cell coloring thing was here. weird why that would be considered marked content.
        // or maybe the stuff from earlier with adding content to parent is sufficient.
        contents.endMarkedContent();
        addContentToParent(COSName.ARTIFACT, null, pages.get(pageIndex), currentElem);
        contents.close();

        //Set up the next marked content element with an MCID and create the containing P structure element.
        contents = new PDPageContentStream(
                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));

        //Draws the given text centered within the current table cell.
        drawCellText(textCell, x + 5, y + height + textCell.getFontSize(), contents);

        //End the marked content and append it's P structure element to the containing P structure element.
        contents.endMarkedContent();
        addContentToParent(COSName.P, null, pages.get(pageIndex), currentElem);
        contents.close();
        return currentElem;
    }

    //Given a DataTable will draw each cell and any given text.
    public void drawDataTable(DataTable table, float x, float y, int pageIndex, PDStructureElement parent) throws IOException {

        COSDictionary attr = new COSDictionary();
        attr.setName(COSName.O, "Table");
        attr.setString(COSName.getPDFName("Summary"), table.getSummary());
        //Create a stream for drawing table's contents and append table structure element to the current form's structure element.
        PDStructureElement currentTable = addContentToParent(null, StandardStructureTypes.TABLE, pages.get(pageIndex), parent);
        currentTable.getCOSObject().setItem(COSName.A, attr);
        currentTable.setAlternateDescription(table.getSummary());

        //Go through each row and add a TR structure element to the table structure element.
        for (int i = 0; i < table.getRows().size(); i++) {

            //Go through each column and draw the cell and any cell's text with given alignment.
            PDStructureElement currentTR = addContentToParent(null, StandardStructureTypes.TR, pages.get(pageIndex), currentTable);
            Row currentRow = table.getRows().get(i);

            for(int j = 0; j < table.getRows().get(i).getCells().size(); j++) {

                Cell currentCell = table.getCell(i, j);
                float cellX = x + currentRow.getCellPosition(j);
                float cellY = y + table.getRowPosition(i);
                addTableCellMarkup(currentCell, pageIndex, currentTR);
                drawCellContents(pageIndex, currentRow, currentCell, cellX, cellY);
                if (!currentCell.getRbVal().isEmpty() || !currentCell.getTextVal().isEmpty()) {
                    if (!currentCell.getTextVal().isEmpty()) {
                        currentCell.setTextVal(currentCell.getTextVal() + " " + table.getId() + " Row " + (i + 1) + " Text Box");
                    }
                    drawCellWidget(pageIndex, currentRow, currentCell, cellX, cellY);
                }

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


    //Add a structure element to a parent structure element with optional marked content given a non-null name param.
    private PDStructureElement addContentToParent(COSName name, String type, PDPage currentPage, PDStructureElement parent) {
        //Create a structure element and add it to the current section.
        PDStructureElement structureElement = null;
        if (type != null) {
            structureElement = new PDStructureElement(type, parent);
            structureElement.setPage(currentPage);
        }
        //If COSName is not null then there is marked content.
        if (name != null) {
            //numDict for parent tree
            COSDictionary numDict = new COSDictionary();
            numDict.setInt(COSName.K, currentMCID - 1);
            numDict.setString(COSName.LANG, "EN-US");
            numDict.setItem(COSName.PG, currentPage.getCOSObject());
            if (structureElement != null) {
                if (!COSName.ARTIFACT.equals(name)) {
                    structureElement.appendKid(new PDMarkedContent(name, currentMarkedContentDictionary));
                } else {
                    structureElement.appendKid(new PDArtifactMarkedContent(currentMarkedContentDictionary));
                }
                numDict.setItem(COSName.P, structureElement.getCOSObject());
            } else {
                if (!COSName.ARTIFACT.equals(name)) {
                    parent.appendKid(new PDMarkedContent(name, currentMarkedContentDictionary));
                } else {
                    parent.appendKid(new PDArtifactMarkedContent(currentMarkedContentDictionary));
                }
                numDict.setItem(COSName.P, parent.getCOSObject());
            }
            numDict.setName(COSName.S, name.getName());
            numDictionaries.add(numDict);
        }
        if (structureElement != null) {
            parent.appendKid(structureElement);
        }
        return structureElement;
    }

    //Add a text box at a given location starting from the top-left corner.
    private void addTextField(float x, float y, float width, float height, String name, int pageIndex) throws IOException {

        PDRectangle rect = new PDRectangle(x, PAGE_HEIGHT - height - y, width, height);

        //Create the form field and add it to the acroForm object with a given name.
        fields.add(new PDTextField(acroForm));
        fields.get(fields.size() - 1).setPartialName(name);
        fields.get(fields.size() - 1).setAlternateFieldName(name);
        ((PDTextField)fields.get(fields.size() - 1)).setMultiline(true);

        // Specify a widget associated with the field.
        PDAnnotationWidget widget = new PDAnnotationWidget();
        PDAppearanceCharacteristicsDictionary fieldAppearance
                = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
        fieldAppearance.setBorderColour(PDConstants.BLACK);
        fieldAppearance.setBackground(PDConstants.WHITE);
        widget.setAppearanceCharacteristics(fieldAppearance);
        widget.setPage(pages.get(pageIndex));
        widget.setAnnotationName(name);
        widget.setParent(((PDTextField)fields.get(fields.size() - 1)));
        widget.setRectangle(rect);

        //Add object reference to widget for tagging purposes.
        PDObjectReference objectReference = new PDObjectReference();
        objectReference.setReferencedObject(widget);
        annotationRefs.add(objectReference);
        widget.getCOSObject().setInt(COSName.STRUCT_PARENT, currentStructParent);
        currentStructParent++;

        //Add the widget to the page.
        widgets.add(widget);
        pages.get(pageIndex).getAnnotations().add(widgets.get(widgets.size() - 1));
        ((PDTextField)fields.get(fields.size() - 1)).setWidgets(Collections.singletonList(widgets.get(widgets.size() - 1)));
        acroForm.getFields().add(fields.get(fields.size() - 1));
    }


    private void addWidgetContent(PDObjectReference objectReference, PDStructureElement fieldElem, String type, int pageIndex) {
        COSDictionary annotDict = new COSDictionary();
        COSArray annotArray = new COSArray();
        annotArray.add(COSInteger.get(currentMCID));
        annotArray.add(objectReference);
        annotDict.setItem(COSName.K, annotArray);
        annotDict.setString(COSName.LANG, "EN-US");
        annotDict.setItem(COSName.P, currentElem.getCOSObject());
        annotDict.setItem(COSName.PG, pages.get(pageIndex).getCOSObject());
        annotDict.setName(COSName.S, type);
        annotDicts.add(annotDict);

        setNextMarkedContentDictionary();
        numDictionaries.add(annotDict);
        fieldElem.appendKid(objectReference);
        currentElem.appendKid(fieldElem);
    }

    private void addTableCellMarkup(Cell cell, int pageIndex, PDStructureElement currentRow) {
        COSDictionary cellAttr = new COSDictionary();
        cellAttr.setName(COSName.O, "Table");
        if (cell.getCellMarkup().isHeader()) {
            currentElem = addContentToParent(null, StandardStructureTypes.TH, pages.get(pageIndex), currentRow);
            currentElem.getCOSObject().setString(COSName.ID, cell.getCellMarkup().getId());
            if (cell.getCellMarkup().getScope().length() > 0) {
                cellAttr.setName(COSName.getPDFName("Scope"), cell.getCellMarkup().getScope());
            }
            if (cell.getCellMarkup().getColspan() > 1) {
                cellAttr.setInt(COSName.getPDFName("ColSpan"), cell.getCellMarkup().getColspan());
            }
            if (cell.getCellMarkup().getRowSpan() > 1) {
                cellAttr.setInt(COSName.getPDFName("RowSpan"), cell.getCellMarkup().getRowSpan());
            }
        } else {
            currentElem = addContentToParent(null, StandardStructureTypes.TD, pages.get(pageIndex), currentRow);
        }
        if (cell.getCellMarkup().getHeaders().length > 0) {
            COSArray headerA = new COSArray();
            for (String s : cell.getCellMarkup().getHeaders()) {
                headerA.add(new COSString(s));
            }
            cellAttr.setItem(COSName.getPDFName("Headers"), headerA);
        }
        currentElem.getCOSObject().setItem(COSName.A, cellAttr);
    }

    private void drawCellContents(int pageIndex, Row currentRow, Cell currentCell, float cellX, float cellY) throws IOException {
        //Set up the next marked content element with an MCID and create the containing TH or TD structure element.
        PDPageContentStream contents = new PDPageContentStream(
                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.ARTIFACT, PDPropertyList.create(currentMarkedContentDictionary));
        contents.endMarkedContent();
        currentElem = addContentToParent(COSName.ARTIFACT, StandardStructureTypes.P, pages.get(pageIndex), currentElem);
        currentElem.setAlternateDescription(currentCell.getText());
        contents.close();

        //Draw the cell's text with a given alignment
        contents = new PDPageContentStream(
                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false);
        setNextMarkedContentDictionary();
        contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));
        switch (currentCell.getAlign()) {
            case PDConstants.CENTER_ALIGN:
                drawCellText(currentCell,
                        cellX + currentCell.getWidth() / 2 - currentCell.getFontSize() / 3.75f * currentCell.getText().length(),
                        cellY + currentRow.getHeight() / 2 + currentCell.getFontSize() / 4,
                        contents);
                break;
            case PDConstants.TOP_ALIGN:
                drawCellText(currentCell,
                        cellX + 5,
                        cellY + currentCell.getFontSize() / 4 + 5,
                        contents);
                break;
            case PDConstants.LEFT_ALIGN:
                drawCellText(currentCell,
                        cellX + 5,
                        cellY + currentRow.getHeight() / 2 + currentCell.getFontSize() / 4,
                        contents);
                break;
        }

        //End the marked content and append it's P structure element to the containing TD structure element.
        contents.endMarkedContent();
        addContentToParent(COSName.P, null, pages.get(pageIndex), currentElem);
        contents.close();
    }



    //Add text at a given location starting from the top-left corner.
    private void drawCellText(Cell cell, float x, float y, PDPageContentStream contents) throws IOException {
        //Open up a stream to draw text at a given location.
        contents.beginText();
        contents.setFont(defaultFont, cell.getFontSize());
        contents.newLineAtOffset(x, PAGE_HEIGHT - y);
        contents.setNonStrokingColor(cell.getTextColor());
        String[] lines = cell.getText().split("\n");
        for (String s: lines) {
            contents.showText(s);
            contents.newLineAtOffset(0, -(cell.getFontSize() * 2));
        }
        contents.endText();
    }

    private void drawCellWidget(int pageIndex, Row currentRow, Cell currentCell, float cellX, float cellY) throws IOException {

        PDStructureElement fieldElem = new PDStructureElement(StandardStructureTypes.FORM, currentElem);
        fieldElem.setPage(pages.get(pageIndex));
        COSArray kArray = new COSArray();
        kArray.add(COSInteger.get(currentMCID));
        fieldElem.getCOSObject().setItem(COSName.K, kArray);

        //Add a text field in the current cell.
        if (!currentCell.getTextVal().isEmpty()) {
            addTextField(cellX, cellY,
                    currentCell.getWidth(),
                    currentRow.getHeight(),
                    currentCell.getTextVal(),
                    pageIndex);
            fieldElem.setAlternateDescription(currentCell.getTextVal());
            addWidgetContent(annotationRefs.get(annotationRefs.size() - 1), fieldElem, StandardStructureTypes.FORM, pageIndex);
        }

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
        documentCatalog.setAcroForm(acroForm);
        PDStructureTreeRoot structureTreeRoot = new PDStructureTreeRoot();
        documentCatalog.setStructureTreeRoot(structureTreeRoot);
        PDMarkInfo markInfo = new PDMarkInfo();
        markInfo.setMarked(true);
        documentCatalog.setMarkInfo(markInfo);
    }

    private PDResources setupAcroForm() throws IOException {
        //Set AcroForm Appearance Characteristics
        PDResources resources = new PDResources();
        defaultFont = PDType0Font.load(pdf,
                new PDTrueTypeFont(PDType1Font.HELVETICA.getCOSObject()).getTrueTypeFont(), true);
        resources.put(COSName.getPDFName("Helv"), defaultFont);
        acroForm.setNeedAppearances(true);
        acroForm.setXFA(null);
        acroForm.setFields(Collections.emptyList());
        acroForm.setDefaultResources(resources);
        acroForm.setDefaultAppearance("/Helv 10 Tf 0 g");
        return resources;
    }


    private void initiatePages(int initPages, PDResources resources) {
        //Create document initial pages
        COSArray cosArray = new COSArray();
        cosArray.add(COSName.getPDFName("PDF"));
        cosArray.add(COSName.getPDFName("Text"));
        COSArray boxArray = new COSArray();
        boxArray.add(new COSFloat(0.0f));
        boxArray.add(new COSFloat(0.0f));
        boxArray.add(new COSFloat(612.0f));
        boxArray.add(new COSFloat(792.0f));
        for (int i = 0; i < initPages; i++) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            page.getCOSObject().setItem(COSName.getPDFName("Tabs"), COSName.S);
            page.setResources(resources);
            page.getResources().getCOSObject().setItem(COSName.PROC_SET, cosArray);
            page.getCOSObject().setItem(COSName.CROP_BOX, boxArray);
            page.getCOSObject().setItem(COSName.ROTATE, COSInteger.get(0));
            page.getCOSObject().setItem(COSName.STRUCT_PARENTS, COSInteger.get(0));
            pages.add(page);
            pdf.addPage(pages.get(pages.size() - 1));
        }
        nums.add(COSInteger.get(0));
    }

    //Adds the parent tree to root struct element to identify tagged content
    private void addParentTree() {
        COSDictionary dict = new COSDictionary();
        nums.add(numDictionaries);
        for (int i = 1; i < currentStructParent; i++) {
            nums.add(COSInteger.get(i));
            nums.add(annotDicts.get(i - 1));
        }
        dict.setItem(COSName.NUMS, nums);
        PDNumberTreeNode numberTreeNode = new PDNumberTreeNode(dict, dict.getClass());
        pdf.getDocumentCatalog().getStructureTreeRoot().setParentTreeNextKey(currentStructParent);
        pdf.getDocumentCatalog().getStructureTreeRoot().setParentTree(numberTreeNode);
        pdf.getDocumentCatalog().getStructureTreeRoot().appendKid(rootElem);
    }

}
