package com.wi.test.app;

import com.wi.test.constants.PDConstants;
import com.wi.test.pojo.TableCellMarkup;
import com.wi.test.util.PDFormBuilder;
import com.wi.test.pojo.Cell;
import com.wi.test.pojo.DataTable;
import com.wi.test.pojo.Row;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.xmpbox.schema.XmpSchemaException;

import javax.xml.transform.TransformerException;
import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
        System.out.println(dateFormat.format(new Date()));
        try {

            //Hard coded table1
            PDFormBuilder formBuilder = new PDFormBuilder(1, "UA EXAMPLE");
            PDStructureElement sec1 = formBuilder.addRoot(0);
            formBuilder.drawElement(
                    new Cell("PDF HEADER 1", new Color(229, 229, 229),
                            Color.BLUE.darker().darker(), 12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
                    50, 25, 50, sec1, StandardStructureTypes.H1, 0);
            DataTable table1 = new DataTable("Table Summary 1", "Table1");
            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 1(ID):", 5, 100, PDConstants.LEFT_ALIGN,  "", new TableCellMarkup("Row", "Table1Row1")),
                    new Cell("56-8987", 5, 400, PDConstants.LEFT_ALIGN, "", new TableCellMarkup(new String[]{"Table1Row1"}))),
                    15));
            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 2(Name):", 5, 100, PDConstants.LEFT_ALIGN, "", new TableCellMarkup("Row", "Table1Row2")),
                    new Cell("Some name", 5, 400, PDConstants.LEFT_ALIGN, "", new TableCellMarkup(new String[]{"Table1Row2"}))),
                    15));
            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 3(Date):", 5, 100, PDConstants.LEFT_ALIGN, "", new TableCellMarkup("Row", "Table1Row3")),
                    new Cell("12/31/2016", 5, 400, PDConstants.LEFT_ALIGN, "", new TableCellMarkup(new String[]{"Table1Row3"}))),
                    15));

            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 9(Input):", 5, 100, PDConstants.LEFT_ALIGN, "", new TableCellMarkup("Row", "Table1Row9")),
                    new Cell("", 5, 300, PDConstants.LEFT_ALIGN, "Row Header 9", new TableCellMarkup(new String[]{"Table1Row9"}))),
                    15));
            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 10(Input):", 5, 100, PDConstants.LEFT_ALIGN, "", new TableCellMarkup("Row", "Table1Row10")),
                    new Cell("", 5, 300, PDConstants.LEFT_ALIGN, "Row Header 10", new TableCellMarkup(new String[]{"Table1Row10"}))),
                    15));
            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 11(Input):", 5, 100, PDConstants.LEFT_ALIGN, "", new TableCellMarkup("Row", "Table1Row11")),
                    new Cell("", 5, 300, PDConstants.LEFT_ALIGN, "Row Header 11", new TableCellMarkup(new String[]{"Table1Row11"}))),
                    15));
            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 12(Input):", 5, 100, PDConstants.LEFT_ALIGN, "", new TableCellMarkup("Row", "Table1Row12")),
                    new Cell("", 5, 300, PDConstants.LEFT_ALIGN, "Row Header 12", new TableCellMarkup(new String[]{"Table1Row12"}))),
                    15));
            table1.addRow(new Row(Arrays.asList(
                    new Cell("Row Header 13(Input):", 5, 100, PDConstants.LEFT_ALIGN, "", new TableCellMarkup("Row", "Table1Row13")),
                    new Cell("", 5, 300, PDConstants.LEFT_ALIGN, "Row Header 13", new TableCellMarkup(new String[]{"Table1Row13"}))),
                    15));
            formBuilder.drawDataTable(table1, 50, 100, 0, sec1);

            //Hard coded table2
            DataTable table2 = new DataTable("Table Summary 2", "Table2");
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Column \nHeader \n1 (Header)", Color.lightGray, 5, 35, PDConstants.TOP_ALIGN, new TableCellMarkup(1, "Column", "Table2Column1")),
                    new Cell("Column \nHeader \n2 (Description)", Color.lightGray, 5, 215, PDConstants.TOP_ALIGN,  new TableCellMarkup(1, "Column", "Table2Column2")),
                    new Cell("Column \nHeader \n3 (Text)", Color.lightGray, 5, 75, PDConstants.TOP_ALIGN, new TableCellMarkup(1, "Column", "Table2Column3")),
                    new Cell("Column \nHeader \n4 (Yes, No, N/A)", Color.lightGray, 5, 60, PDConstants.TOP_ALIGN, new TableCellMarkup(3, "Column", "Table2Column4")),
                    new Cell("Column \nHeader \n5 (Comments)", Color.lightGray, 5, 100, PDConstants.TOP_ALIGN, new TableCellMarkup(1, "Column", "Table2Column5"))),
                    Collections.emptyList(), "", 30));
            table2.addRow(new Row(Arrays.asList(
                    new Cell("", Color.lightGray, 5, 35, PDConstants.CENTER_ALIGN, new TableCellMarkup()),
                    new Cell("", Color.lightGray, 5, 215, PDConstants.CENTER_ALIGN, new TableCellMarkup()),
                    new Cell("", Color.lightGray, 5, 75, PDConstants.CENTER_ALIGN, new TableCellMarkup()),
                    new Cell("Yes", Color.lightGray, 5, 20, PDConstants.CENTER_ALIGN, new TableCellMarkup(new String[]{"Review"}, "Yes")),
                    new Cell("No", Color.lightGray, 5, 20, PDConstants.CENTER_ALIGN, new TableCellMarkup(new String[]{"Review"}, "No")),
                    new Cell("N/A", Color.lightGray, 5, 20, PDConstants.CENTER_ALIGN, new TableCellMarkup(new String[]{"Review"}, "N/A")),
                    new Cell("", Color.lightGray, 5, 100, PDConstants.CENTER_ALIGN, new TableCellMarkup())),
                    Collections.emptyList(), "", 20));
            table2.addRow(new Row(Collections.singletonList(
                    new Cell("SECTION HEADER 1", 6, 485, PDConstants.LEFT_ALIGN, new TableCellMarkup(7, "Column", "SECTION1"))),
                    Collections.emptyList(), "", 20));
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Row \nHeader \n1", 5, 35, PDConstants.TOP_ALIGN, new TableCellMarkup("Row", new String[]{"Table2Column1"}, "Table2Row1")),
                    new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "Goodbye.",
                            5, 215, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column2", "Table2Row1"})),
                    new Cell("System Verification: N/A.", 5, 75, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column3", "Table2Row1"})),
                    new Cell("", 5, 20, PDConstants.TOP_ALIGN, "Yes", "",  new TableCellMarkup(new String[]{"Table2Column4", "Table2Row1", "Yes"})),
                    new Cell("", 5, 20, PDConstants.TOP_ALIGN, "No", "",  new TableCellMarkup(new String[]{"Table2Column4", "Table2Row1", "No"})),
                    new Cell("", 5, 20, PDConstants.TOP_ALIGN, "N/A", "", new TableCellMarkup(new String[]{"Table2Column4", "Table2Row1", "N/A"})),
                    new Cell("", 5, 100, PDConstants.TOP_ALIGN, "", "Table2", new TableCellMarkup(new String[]{"Table2Column5", "Table2Row1"}))),
                    Arrays.asList("Yes", "No", "N/A"), "Table2", 50));
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Row \nHeader \n2", 5, 35, PDConstants.TOP_ALIGN, new TableCellMarkup("Row", new String[]{"Table2Column1"}, "Table2Row2")),
                    new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "Goodbye.",
                            5, 215, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column2", "Table2Row2"})),
                    new Cell("System Verification: N/A.", 5, 75, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column3", "Table2Row2"})),
                    new Cell("", 5, 20, PDConstants.TOP_ALIGN, "Yes", "",  new TableCellMarkup(new String[]{"Table2Column4", "Table2Row2", "Yes"})),
                    new Cell("", 5, 20, PDConstants.TOP_ALIGN, "No", "",  new TableCellMarkup(new String[]{"Table2Column4", "Table2Row2", "No"})),
                    new Cell("", 5, 20, PDConstants.TOP_ALIGN, "N/A", "", new TableCellMarkup(new String[]{"Table2Column4", "Table2Row2", "N/A"})),
                    new Cell("", 5, 100, PDConstants.TOP_ALIGN, "", "Table2", new TableCellMarkup(new String[]{"Table2Column5", "Table2Row2"}))),
                    Arrays.asList("Yes", "No", "N/A"), "Table2", 40));
            formBuilder.drawDataTable(table2, 50, 310, 0, sec1);
            formBuilder.saveAndClose("UAEXAMPLE.PDF");

        } catch (IOException | TransformerException | XmpSchemaException ex) {
            ex.printStackTrace();
        }
        System.out.println(dateFormat.format(new Date()));
    }
}