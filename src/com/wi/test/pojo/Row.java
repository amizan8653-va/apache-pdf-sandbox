package com.wi.test.pojo;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

import java.util.ArrayList;
import java.util.List;

public class Row {
    private List<Cell> cells = new ArrayList<>();
    private float height = 0;
    private int selectedRadio = -1;
    private List<String> radioValues = new ArrayList<>();
    private List<PDAnnotationWidget> radioWidgets = new ArrayList<>();
    private String radioName = "";

    public Row(List<Cell> cells, List<String> radioValues, String radioName, float height) {
        this.cells = cells;
        this.height = height;
        this.radioValues = radioValues;
        this.radioName = radioName;
    }

    public Row(List<Cell> cells, float height) {
        this.height = height;
        this.cells = cells;
    }

    public Row() { }

    public void addCell(Cell cell) {
        cells.add(cell);
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

    public float getCellPosition(int cellIndex) {
        float currentPosition = 0;
        for (int i = 0; i < cellIndex; i++) {
            currentPosition += cells.get(i).getWidth();
        }
        return currentPosition;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public List<String> getRadioValues() {
        return radioValues;
    }

    public void setRadioValues(List<String> radioValues) {
        this.radioValues = radioValues;
    }

    public String getRadioName() {
        return radioName;
    }

    public void setRadioName(String radioName) {
        this.radioName = radioName;
    }

    public List<PDAnnotationWidget> getRadioWidgets() {
        return radioWidgets;
    }

    public void addRadioWidget(PDAnnotationWidget radioWidget) {
        this.radioWidgets.add(radioWidget);
    }

    public int getSelectedRadio() {
        return selectedRadio;
    }

    public void setSelectedRadio(int selectedRadio) {
        this.selectedRadio = selectedRadio;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "{ \n");
        sb.append("\t\"cells\" : [ ");
        for (Cell c : cells) {
            sb.append(c.toString().replace("\n\t", "\n\t\t").replace("\n}", "\n\t}"));
            sb.append(", ");
        }
        sb.deleteCharAt(sb.lastIndexOf(", "));
        sb.append( "]\n");
        sb.append("}");
        return sb.toString();
    }
}
