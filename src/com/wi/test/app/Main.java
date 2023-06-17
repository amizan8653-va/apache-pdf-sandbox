package com.wi.test.app;

import com.wi.test.constants.PDConstants;
import com.wi.test.enums.Font;
import com.wi.test.enums.TableHeaderType;
import com.wi.test.pojo.PageMargins;
import com.wi.test.pojo.Text;
import com.wi.test.pojo.UpdatedPagePosition;
import com.wi.test.util.CustomTaggedPdfBuilder;
import com.wi.test.pojo.Cell;
import com.wi.test.pojo.DataTable;
import com.wi.test.pojo.Row;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
        System.out.println(dateFormat.format(new Date()));


        List<String> urls = List.of(
            "https://www.va.gov",
            "https://www.va.gov/contact-us",
            "http://www.va.gov/statedva.htm",
            "www.healthcare.gov",
            "https://www.va.gov/health-care/about-affordable-care-act"
        );

        String phoneNumber = "1-800-827-1000";

        CustomTaggedPdfBuilder formBuilder = new CustomTaggedPdfBuilder("UA EXAMPLE", new PageMargins(20,0,20,20));
        PDStructureElement sec1 = formBuilder.getRoot();

        formBuilder.drawTextElement(
            new Text(14, "PDF HEADER 1",Color.BLUE.darker().darker(), Font.HELVETICA_BOLD),
            0, 0, sec1, StandardStructureTypes.H1, 0);

        UpdatedPagePosition newPosition = drawTableOne(formBuilder, sec1);


        //Hard coded table2
        DataTable table2 = new DataTable("Table Summary 2", TableHeaderType.ROW_HEADERS);
        table2.addRow(new Row(Arrays.asList(
                new Cell("Column \nHeader \n1 (Header)", Font.HELVETICA, 10, 45, PDConstants.TOP_ALIGN, true),
                new Cell("Column \nHeader \n2 (Description)", Font.HELVETICA, 10, 215, PDConstants.TOP_ALIGN,  true),
                new Cell("Column \nHeader \n3 (Text)",  Font.HELVETICA, 10, 75, PDConstants.TOP_ALIGN, true))));
        table2.addRow(new Row(Arrays.asList(
                new Cell("Row \nHeader \n1", Font.HELVETICA, 10, 45, PDConstants.TOP_ALIGN, false),
                new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                        "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                        "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                        "Goodbye.",
                    Font.HELVETICA, 10, 215, PDConstants.TOP_ALIGN, false),
                new Cell("System Verification: N/A.", Font.HELVETICA, 10, 75, PDConstants.TOP_ALIGN, false))));
        table2.addRow(new Row(Arrays.asList(
                new Cell("Row \nHeader \n2", Font.HELVETICA, 10, 45, PDConstants.TOP_ALIGN, false),
                new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                        "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                        "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                        "Goodbye.",
                    Font.HELVETICA, 10, 215, PDConstants.TOP_ALIGN, false),
                new Cell("System Verification: N/A.", Font.HELVETICA, 10, 75, PDConstants.TOP_ALIGN, false))));
        table2.addRow(new Row(Arrays.asList(
            new Cell("Row \nHeader \n3", Font.HELVETICA, 10, 45, PDConstants.TOP_ALIGN, false),
            new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                "Goodbye.",
                Font.HELVETICA, 10, 215, PDConstants.TOP_ALIGN, false),
            new Cell("System Verification: N/A.", Font.HELVETICA, 10, 75, PDConstants.TOP_ALIGN, false))));
        table2.addRow(new Row(Arrays.asList(
            new Cell("Row \nHeader \n4", Font.HELVETICA, 10, 45, PDConstants.TOP_ALIGN, false),
            new Cell("Hi. This is a long paragraph about absolutely nothing. I hope you enjoy reading it! \n" +
                "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                "This is a long paragraph about absolutely nothing. I hope you enjoy reading it!\n" +
                "Goodbye.",
                Font.HELVETICA, 10, 215, PDConstants.TOP_ALIGN, false),
            new Cell("System Verification: N/A.", Font.HELVETICA, 10, 75, PDConstants.TOP_ALIGN, false))));
        newPosition = formBuilder.drawTable(table2, 50, newPosition.getY() + 50.0f, newPosition.getPageIndex(), sec1, 5);


        newPosition = formBuilder.drawTextElement(
            new Text(12,
                IntStream.range(0,30)
                    .mapToObj(integer -> String.format("This is a very long string %d. ", integer))
                    .collect(Collectors.joining()),
                Color.BLACK,
                Font.HELVETICA),
            0, newPosition.getY() + 450, sec1, StandardStructureTypes.P, newPosition.getPageIndex());

        List<Text> bulletedList = Stream.of(
            "test item 1",
                "test item 2",
                "test item 3. This will be a very long string that will end up being more than 1 line when rendered. " +
                    "It's not quite there just after that first sentence, but it will be after the 2nd.",
                "test item 4",
                "test item 5. This will be a very long string that will end up being more than 1 line when rendered. " +
                    "It's not quite there just after that first sentence, but it will be after the 2nd.",
                "test item 6",
                "test item 7")
            .map(str -> new Text(12, str, Color.BLACK, Font.HELVETICA))
            .collect(Collectors.toList());
        // test extra x padding, and also test page overflow halfway through bullet point.
        newPosition = formBuilder.drawBulletList(bulletedList, 10, newPosition.getY() + 665.0f, newPosition.getPageIndex(), sec1);

        // test no extra x padding, and also test page overflow at start of new bullet point.
        newPosition = formBuilder.drawBulletList(bulletedList, 0, newPosition.getY() + 600.0f, newPosition.getPageIndex(), sec1);

        newPosition = formBuilder.drawTextElement(
            new Text(12,
                IntStream.range(0,1)
                    .mapToObj(integer -> String.format("This is a very long string %d. Here is a url that will be " +
                        "injected into it: %s.\nHere is a phone number too on a new line: %s. ",
                        integer, urls.get(integer % urls.size()), phoneNumber))
                    .collect(Collectors.joining()),
                Color.BLACK,
                Font.HELVETICA),
            0, newPosition.getY() + 20, sec1, StandardStructureTypes.P, newPosition.getPageIndex());

        formBuilder.saveAndClose("UAEXAMPLE.PDF");

        System.out.println(dateFormat.format(new Date()));
    }

    private static UpdatedPagePosition drawTableOne(CustomTaggedPdfBuilder formBuilder, PDStructureElement sec1) {
        //Hard coded table1
        DataTable table1 = new DataTable("Table Summary 1", TableHeaderType.COLUMN_HEADERS);
        table1.addRow(new Row(Arrays.asList(
            new Cell("Row Header 1 (ID) BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH:", Font.HELVETICA, 10, 100, PDConstants.LEFT_ALIGN,  true),
            new Cell("56-8987 BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH", Font.HELVETICA, 10, 200, PDConstants.LEFT_ALIGN, false))));
        table1.addRow(new Row(Arrays.asList(
            new Cell("Row Header 2 (Name) BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH:", Font.HELVETICA, 10, 100, PDConstants.LEFT_ALIGN, true),
            new Cell("Some name BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH", Font.HELVETICA, 10, 200, PDConstants.LEFT_ALIGN, false))));
        table1.addRow(new Row(Arrays.asList(
            new Cell("Row Header 3 (Date) BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH:", Font.HELVETICA, 10, 100, PDConstants.LEFT_ALIGN, true),
            new Cell("12/31/2016 BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH", Font.HELVETICA, 10, 200, PDConstants.LEFT_ALIGN, false))));
        return formBuilder.drawTable(table1, 50, 25, 0, sec1, 5);
    }
}