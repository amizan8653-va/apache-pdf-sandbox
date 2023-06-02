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
            formBuilder.drawDataTable(table1, 50, 100, 0, sec1);

            //Hard coded table2
            DataTable table2 = new DataTable("Table Summary 2", "Table2");
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Column \nHeader \n1 (Header)", Color.lightGray, 5, 35, PDConstants.TOP_ALIGN, new TableCellMarkup(1, "Column", "Table2Column1")),
                    new Cell("Column \nHeader \n2 (Description)", Color.lightGray, 5, 215, PDConstants.TOP_ALIGN,  new TableCellMarkup(1, "Column", "Table2Column2")),
                    new Cell("Column \nHeader \n3 (Text)", Color.lightGray, 5, 75, PDConstants.TOP_ALIGN, new TableCellMarkup(1, "Column", "Table2Column3"))),
                30));
            table2.addRow(new Row(Collections.singletonList(
                    new Cell("SECTION HEADER 1", 6, 485, PDConstants.LEFT_ALIGN, new TableCellMarkup(7, "Column", "SECTION1"))),
                20));
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Row \nHeader \n1", 5, 35, PDConstants.TOP_ALIGN, new TableCellMarkup("Row", new String[]{"Table2Column1"}, "Table2Row1")),
                    new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "Goodbye.",
                            5, 215, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column2", "Table2Row1"})),
                    new Cell("System Verification: N/A.", 5, 75, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column3", "Table2Row1"}))),
                    50));
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Row \nHeader \n2", 5, 35, PDConstants.TOP_ALIGN, new TableCellMarkup("Row", new String[]{"Table2Column1"}, "Table2Row2")),
                    new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "Goodbye.",
                            5, 215, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column2", "Table2Row2"})),
                    new Cell("System Verification: N/A.", 5, 75, PDConstants.TOP_ALIGN, new TableCellMarkup(new String[]{"Table2Column3", "Table2Row2"}))),

                    40));
            formBuilder.drawDataTable(table2, 50, 310, 0, sec1);
            formBuilder.saveAndClose("UAEXAMPLE.PDF");

        } catch (IOException | TransformerException | XmpSchemaException ex) {
            ex.printStackTrace();
        }
        System.out.println(dateFormat.format(new Date()));
    }
}