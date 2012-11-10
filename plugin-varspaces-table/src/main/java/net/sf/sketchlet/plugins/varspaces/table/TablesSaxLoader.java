/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author zobrenovic
 */
public class TablesSaxLoader extends DefaultHandler {

    TableVariableSpace tables;

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
    String currentElement;
    Table table = null;
    TableColumn column = null;
    Vector<String> rowData = null;

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            // System.out.println("Start element: " + qName);
            strElem = qName;
        } else {
            // System.out.println("Start element: {" + uri + "}" + name);
            strElem = name;
        }

        this.currentElement = strElem;
        this.strCharacters = "";

        if (strElem.equalsIgnoreCase("table")) {
            this.table = new Table();
        } else if (strElem.equalsIgnoreCase("column")) {
            this.column = new TableColumn();
            String str = atts.getValue("name");
            this.column.name = str != null ? str : "";
            str = atts.getValue("type");
            this.column.type = str != null ? str : "";
            str = atts.getValue("defaultValue");
            this.column.defaultValue = str != null ? str : "";
            this.table.columns.add(column);
        } else if (strElem.equalsIgnoreCase("row")) {
            this.rowData = new Vector<String>();
            this.table.data.add(rowData);
        }
    }

    public void endElement(String uri, String name, String qName) {
        String strElem = "";
        if ("".equals(uri)) {
            // System.out.println("End element: " + qName);
            strElem = qName;
        } else {
            // System.out.println("End element:   {" + uri + "}" + name);
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
    String strCharacters = "";

    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            strCharacters += strValue;
        }
    }

    public void processCharacters() {
        strCharacters = strCharacters.replace("\\n", "\n");
        strCharacters = strCharacters.replace("\\r", "\r");
        strCharacters = strCharacters.replace("&lt;", "<");
        strCharacters = strCharacters.replace("&gt;", ">");
        strCharacters = strCharacters.replace("&amp;", "&");
        if (currentElement == null) {
            return;
        }
        if (currentElement.equalsIgnoreCase("name")) {
            table.name = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("col") && rowData != null) {
            rowData.add(strCharacters);
        }
    }
}
