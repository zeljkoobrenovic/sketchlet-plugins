/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table;

import net.sf.sketchlet.common.file.FileUtils;
import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class Table {

    public final static String SEPARATOR = "\t";
    public String name = "";
    public Vector<TableColumn> columns = new Vector<TableColumn>();
    public Vector<Vector<String>> data = new Vector<Vector<String>>();

    public Table() {
    }

    public void delete(int pos) {
        try {
            data.remove(pos);
        } catch (Exception e) {
        }
    }

    public void dropColumn(int index) {
        this.columns.remove(index);

        if (index >= 0) {
            for (Vector<String> row : this.data) {
                if (index < row.size()) {
                    row.removeElementAt(index);
                }
            }
        }
    }

    public void addColumn() {
        for (Vector<String> row : this.data) {
            row.add("");
        }
    }

    public void replaceColumns(int col1, int col2) {
        expandData();
        if (col1 >= 0 && col1 < columns.size() && col2 >= 0 && col2 < columns.size() && col1 != col2) {
            TableColumn column1 = columns.elementAt(col1);
            TableColumn column2 = columns.elementAt(col2);
            columns.setElementAt(column2, col1);
            columns.setElementAt(column1, col2);
            int i = 0;
            for (Vector<String> row : this.data) {
                String value1 = row.elementAt(col1);
                String value2 = row.elementAt(col2);
                row.setElementAt(value2, col1);
                row.setElementAt(value1, col2);
            }
        }
    }

    public void save(PrintWriter out) {
        try {
            out.println("<table>");
            out.println("    <meta>");
            out.println("       <name>" + this.name + "</name>");
            out.println("       <structure>");
            for (TableColumn col : columns) {
                out.println("          " + col.toXML());
            }
            out.println("       </structure>");
            out.println("    </meta>");
            out.println("    <data>");
            for (Vector<String> row : data) {
                String strRow = "";
                out.print("        <row>");
                for (String value : row) {
                    out.print("<col>" + value + "</col>");
                }
                out.print(strRow);
                out.println("    </row>");
            }
            out.println("    </data>");
            out.println("</table>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Vector<Vector<String>> getValue(String id) {
        return null;
    }

    public TableColumn addNewColumn() {
        TableColumn tableColumn = new TableColumn();
        return this.addColumn(tableColumn);
    }

    public TableColumn addColumn(TableColumn tableColumn) {
        this.columns.add(tableColumn);
        expandData();
        return tableColumn;
    }

    public void addEmptyRow() {
        Vector<String> newRow = new Vector<String>();
        for (int i = 0; i < columns.size(); i++) {
            TableColumn col = columns.elementAt(i);
            newRow.add(col.defaultValue);
        }

        data.add(newRow);
    }

    public void addRows(String strData) {
        String rows[] = strData.split("\n");
        for (String row : rows) {
            this.add(row);
        }
        expandData();
    }

    public void add(String strRow) {
        add(strRow.split(Table.SEPARATOR));
    }

    public void expandData() {
        for (Vector<String> row : data) {
            for (int i = row.size(); i < columns.size(); i++) {
                row.add(columns.elementAt(i).defaultValue);
            }
        }
    }

    public void add(String rowData[], int cols[]) {
        Vector<String> newRow = new Vector<String>();

        for (int i = 0; i < Math.min(rowData.length, cols.length); i++) {
            if (cols[i] >= 0 && cols[i] < this.columns.size()) {
                TableColumn col = this.columns.elementAt(cols[i]);
                if (col != null) {
                    newRow.add(col.prepareValue(rowData[i]));
                }
            }
        }

        data.add(newRow);
    }

    public void update(int row, String rowData[], int cols[]) {
        if (row >= 0 && row < data.size()) {
            Vector<String> rowVector = data.elementAt(row);

            for (int i = 0; i < Math.min(rowData.length, cols.length); i++) {
                if (cols[i] >= 0 && cols[i] < this.columns.size()) {
                    TableColumn col = this.columns.elementAt(cols[i]);
                    if (col != null) {
                        rowVector.setElementAt(col.prepareValue(rowData[i]), cols[i]);
                    }
                }
            }
        }
    }

    public TableColumn getColumnByName(String colName) {
        for (TableColumn tc : this.columns) {
            if (tc.name.equalsIgnoreCase(colName)) {
                return tc;
            }
        }

        return null;
    }

    public void add(String row[]) {
        Vector<String> newRow = new Vector<String>();
        for (int i = 0; i < columns.size(); i++) {
            TableColumn col = columns.elementAt(i);
            String value = i < row.length ? row[i] : "";
            newRow.add(col.prepareValue(value));
        }

        data.add(newRow);
    }

    public void add(Vector<String> row) {
        Vector<String> newRow;
        if (row.size() != columns.size()) {
            newRow = new Vector<String>();
            for (int i = 0; i < columns.size(); i++) {
                TableColumn col = columns.elementAt(i);
                String value = i < row.size() ? row.elementAt(i) : "";
                newRow.add(col.prepareValue(value));
            }
        } else {
            newRow = row;
        }

        data.add(newRow);
    }

    public void restructure(Vector<Vector<String>> originalData) {
        this.data.removeAllElements();
        for (Vector<String> row : originalData) {
            this.add(row);
        }
    }

    public static void main(String args[]) {
        File dir = new File("c:\\temp\\tables");
        FileUtils.deleteDir(dir);
        Table t = new Table();
        t.name = "person";
        t.columns.add(new TableColumn("name\tString\tJohn"));
        t.columns.add(new TableColumn("address\tString\tJohn"));

        t.add("A\tAmsterdam");
        t.add("B\tAmsterdam");

        //t.onSave(dir);

        TableVariableSpace tds = new TableVariableSpace();
        tds.afterProjectOpening();

        for (Table table : tds.tablesVector) {
            table.name += "_new";
            //table.onSave(dir);
        }
    }

    public String toString() {
        StringBuffer str = new StringBuffer("");

        for (Vector<String> row : this.data) {
            int i = 0;
            for (String cell : row) {
                if (i > 0) {
                    str.append("\t");
                }
                str.append(cell);
                i++;
            }
            str.append("\n");
        }

        return str.toString();
    }

    public String getUniqueColumnName() {
        String namePrefix = "Column";

        int i = 1;
        String name = namePrefix + i;
        while (this.columnExists(name)) {
            i++;
            name = namePrefix + i;
        }

        return name;
    }

    public boolean columnExists(String colName) {
        for (TableColumn tc : this.columns) {
            if (tc.name.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public String getExpressionExamples() {
        StringBuffer str = new StringBuffer("");

        str.append("Get all data from the table:\n");
        str.append("=@table." + this.name + "\n");
        str.append("\n");

        str.append("Get data from the column '" + this.columns.elementAt(0).name + "':\n");
        str.append("=@table." + this.name + "." + this.columns.elementAt(0).name + "\n");
        str.append("\n");

        if (this.columns.size() > 1) {
            str.append("Get data from two columns:\n");
            str.append("=@table." + this.name + "." + this.columns.elementAt(0).name + "," + this.columns.elementAt(1).name + "\n");
            str.append("\n");
        }

        /*if (this.columns.size() > 1) {
        str.append("Get data from first row:\n");
        str.append("=@table." + this.name + ".1\n");
        str.append("\n");
        }*/

        str.append("Get data from first row, column '" + this.columns.elementAt(0).name + "':\n");
        str.append("=@table." + this.name + "." + this.columns.elementAt(0).name + ".1\n");
        str.append("\n");

        str.append("Get data from first columns, rows 1 to 10:\n");
        str.append("=@table." + this.name + "." + this.columns.elementAt(0).name + ".1-10\n");
        str.append("\n");

        str.append("To add a new row, use the 'Variable update' action:\n");
        str.append("    Variable name: @table." + this.name + "\n");
        str.append("    Variable name: @table." + this.name + ".addrow\n");
        str.append("    Param1: A\\tB\\tC\n");
        str.append("        Use '\\t' to separate values of each column.\n");
        str.append("\n");

        str.append("To update an existing row, use the 'Variable update' action:\n");
        str.append("    Variable name: @table." + this.name + "." + this.columns.elementAt(0).name + ".1\n");
        str.append("    Param1: A\n");
        str.append("        Use '\\t' to separate values of each column.\n");
        str.append("\n");

        return str.toString();
    }
}
