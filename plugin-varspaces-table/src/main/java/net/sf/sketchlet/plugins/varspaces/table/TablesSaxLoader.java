/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zobrenovic
 */
public class TablesSaxLoader extends DefaultHandler {

    private String currentElement;
    private Table table = null;
    private TableColumn column = null;
    private List<String> rowData = null;
    private TableVariableSpace tables;
    private String characters = "";

    public TablesSaxLoader(TableVariableSpace tables) {
        super();
        this.tables = tables;
    }

    public static void loadTable(File file, TableVariableSpace tables) {
        TablesSaxLoader handler = new TablesSaxLoader(tables);
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            FileReader r = new FileReader(file);
            xr.parse(new InputSource(r));
            r.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        this.currentElement = strElem;
        this.characters = "";

        if (strElem.equalsIgnoreCase("table")) {
            this.table = new Table();
        } else if (strElem.equalsIgnoreCase("column")) {
            this.column = new TableColumn();
            String str = atts.getValue("name");
            this.column.setName(str != null ? str : "");
            str = atts.getValue("type");
            this.column.setType(str != null ? str : "");
            str = atts.getValue("defaultValue");
            this.column.setDefaultValue(str != null ? str : "");
            this.table.getColumns().add(column);
        } else if (strElem.equalsIgnoreCase("row")) {
            this.rowData = new ArrayList<String>();
            this.table.getData().add(rowData);
        }
    }

    public void endElement(String uri, String name, String qName) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        this.processCharacters();

        currentElement = null;
        if (strElem.equalsIgnoreCase("table")) {
            this.tables.addTable(table);
            this.table = null;
        } else if (strElem.equalsIgnoreCase("column")) {
            this.column = null;
        } else if (strElem.equalsIgnoreCase("row")) {
            this.rowData = null;
        }
    }

    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            characters += strValue;
        }
    }

    private void processCharacters() {
        characters = characters.replace("\\n", "\n");
        characters = characters.replace("\\r", "\r");
        characters = characters.replace("&lt;", "<");
        characters = characters.replace("&gt;", ">");
        characters = characters.replace("&amp;", "&");
        if (currentElement == null) {
            return;
        }
        if (currentElement.equalsIgnoreCase("name")) {
            table.setName(characters);
        }
        if (currentElement.equalsIgnoreCase("col") && rowData != null) {
            rowData.add(characters);
        }
    }
}
