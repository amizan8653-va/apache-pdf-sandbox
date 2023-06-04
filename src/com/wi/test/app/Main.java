package com.wi.test.app;

import com.wi.test.constants.PDConstants;
import com.wi.test.enums.Font;
import com.wi.test.pojo.Text;
import com.wi.test.util.CustomTaggedPdfBuilder;
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
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
        System.out.println(dateFormat.format(new Date()));
        try {

            CustomTaggedPdfBuilder formBuilder = new CustomTaggedPdfBuilder(1, "UA EXAMPLE");
            PDStructureElement sec1 = formBuilder.addRoot(0);

            formBuilder.drawTextElement(
                new Cell("PDF HEADER 1", Font.HELVETICA_BOLD,
                    Color.BLUE.darker().darker(), 12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
                50, 25, 50, sec1, StandardStructureTypes.H1, 0);

            drawTableOne(formBuilder, sec1);


            //Hard coded table2
            DataTable table2 = new DataTable("Table Summary 2");
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Column \nHeader \n1 (Header)", Font.HELVETICA, 5, 35, PDConstants.TOP_ALIGN, true),
                    new Cell("Column \nHeader \n2 (Description)", Font.HELVETICA, 5, 215, PDConstants.TOP_ALIGN,  true),
                    new Cell("Column \nHeader \n3 (Text)",  Font.HELVETICA, 5, 75, PDConstants.TOP_ALIGN, true)),
                30));
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Row \nHeader \n1", Font.HELVETICA, 5, 35, PDConstants.TOP_ALIGN, false),
                    new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "Goodbye.",
                        Font.HELVETICA, 5, 215, PDConstants.TOP_ALIGN, false),
                    new Cell("System Verification: N/A.", Font.HELVETICA, 5, 75, PDConstants.TOP_ALIGN, false)),
                    50));
            table2.addRow(new Row(Arrays.asList(
                    new Cell("Row \nHeader \n2", Font.HELVETICA, 5, 35, PDConstants.TOP_ALIGN, false),
                    new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                            "Goodbye.",
                        Font.HELVETICA, 5, 215, PDConstants.TOP_ALIGN, false),
                    new Cell("System Verification: N/A.", Font.HELVETICA, 5, 75, PDConstants.TOP_ALIGN, false)),

                    40));
            formBuilder.drawDataTable(table2, 50, 150, 0, sec1);

            // draw a bulleted list and try to tag it.

            formBuilder.saveAndClose("UAEXAMPLE.PDF");

        } catch (IOException | TransformerException | XmpSchemaException ex) {
            ex.printStackTrace();
        }
        System.out.println(dateFormat.format(new Date()));
    }

    private static void drawTableOne(CustomTaggedPdfBuilder formBuilder, PDStructureElement sec1) throws IOException, TransformerException, XmpSchemaException {
        //Hard coded table1
        DataTable table1 = new DataTable("Table Summary 1");
        table1.addRow(new Row(Arrays.asList(
            new Cell("Row Header 1(ID):", Font.HELVETICA, 5, 100, PDConstants.LEFT_ALIGN,  true),
            new Cell("56-8987", Font.HELVETICA, 5, 400, PDConstants.LEFT_ALIGN, false)),
            15));
        table1.addRow(new Row(Arrays.asList(
            new Cell("Row Header 2(Name):", Font.HELVETICA, 5, 100, PDConstants.LEFT_ALIGN, true),
            new Cell("Some name", Font.HELVETICA, 5, 400, PDConstants.LEFT_ALIGN, false)),
            15));
        table1.addRow(new Row(Arrays.asList(
            new Cell("Row Header 3(Date):", Font.HELVETICA, 5, 100, PDConstants.LEFT_ALIGN, true),
            new Cell("12/31/2016", Font.HELVETICA, 5, 400, PDConstants.LEFT_ALIGN, false)),
            15));
        formBuilder.drawDataTable(table1, 50, 100, 0, sec1);
    }
}