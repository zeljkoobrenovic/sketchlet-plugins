/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zobrenovic
 */
public class Table {

    public final static String SEPARATOR = "\t";
    private String name = "";
    private List<TableColumn> columns = new ArrayList<TableColumn>();
    private List<List<String>> data = new ArrayList<List<String>>();

    public Table() {
    }

    public void dropColumn(int index) {
        this.getColumns().remove(index);

        if (index >= 0) {
            for (List<String> row : this.getData()) {
                if (index < row.size()) {
                    row.get(index);
                }
            }
        }
    }

    public void replaceColumns(int col1, int col2) {
        expandData();
        if (col1 >= 0 && col1 < getColumns().size() && col2 >= 0 && col2 < getColumns().size() && col1 != col2) {
            TableColumn column1 = getColumns().get(col1);
            TableColumn column2 = getColumns().get(col2);
            getColumns().set(col1, column2);
            getColumns().set(col2, column1);
            int i = 0;
            for (List<String> row : this.getData()) {
                String value1 = row.get(col1);
                String value2 = row.get(col2);
                row.set(col1, value2);
                row.set(col2, value1);
            }
        }
    }

    protected void save(PrintWriter out) {
        try {
            out.println("<table>");
            out.println("    <meta>");
            out.println("       <name>" + this.getName() + "</name>");
            out.println("       <structure>");
            for (TableColumn col : getColumns()) {
                out.println("          " + col.toXML());
            }
            out.println("       </structure>");
            out.println("    </meta>");
            out.println("    <data>");
            for (List<String> row : getData()) {
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

    public TableColumn addNewColumn() {
        TableColumn tableColumn = new TableColumn();
        return this.addColumn(tableColumn);
    }

    public TableColumn addColumn(TableColumn tableColumn) {
        this.getColumns().add(tableColumn);
        expandData();
        return tableColumn;
    }

    public void addEmptyRow() {
        List<String> newRow = new ArrayList<String>();
        for (int i = 0; i < getColumns().size(); i++) {
            TableColumn col = getColumns().get(i);
            newRow.add(col.getDefaultValue());
        }

        getData().add(newRow);
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

    private void expandData() {
        for (List<String> row : getData()) {
            for (int i = row.size(); i < getColumns().size(); i++) {
                row.add(getColumns().get(i).getDefaultValue());
            }
        }
    }

    void add(String rowData[], int cols[]) {
        List<String> newRow = new ArrayList<String>();

        for (int i = 0; i < Math.min(rowData.length, cols.length); i++) {
            if (cols[i] >= 0 && cols[i] < this.getColumns().size()) {
                TableColumn col = this.getColumns().get(cols[i]);
                if (col != null) {
                    newRow.add(col.prepareValue(rowData[i]));
                }
            }
        }

        getData().add(newRow);
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

    boolean columnExists(String colName) {
        for (TableColumn tc : this.columns) {
            if (tc.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    private void add(String row[]) {
        List<String> newRow = new ArrayList<String>();
        for (int i = 0; i < getColumns().size(); i++) {
            TableColumn col = getColumns().get(i);
            String value = i < row.length ? row[i] : "";
            newRow.add(col.prepareValue(value));
        }

        getData().add(newRow);
    }

    private void add(List<String> row) {
        List<String> newRow;
        if (row.size() != getColumns().size()) {
            newRow = new ArrayList<String>();
            for (int i = 0; i < getColumns().size(); i++) {
                TableColumn col = getColumns().get(i);
                String value = i < row.size() ? row.get(i) : "";
                newRow.add(col.prepareValue(value));
            }
        } else {
            newRow = row;
        }

        getData().add(newRow);
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer("");

        for (List<String> row : this.getData()) {
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

    void update(int row, String rowData[], int cols[]) {
        if (row >= 0 && row < data.size()) {
            List<String> rowVector = data.get(row);

            for (int i = 0; i < Math.min(rowData.length, cols.length); i++) {
                if (cols[i] >= 0 && cols[i] < this.columns.size()) {
                    TableColumn col = this.columns.get(cols[i]);
                    if (col != null) {
                        rowVector.set(cols[i], col.prepareValue(rowData[i]));
                    }
                }
            }
        }
    }

    public String getExpressionExamples() {
        StringBuffer str = new StringBuffer("");

        str.append("Get all data from the table:\n");
        str.append("=@table." + this.getName() + "\n");
        str.append("\n");

        str.append("Get data from the column '" + this.getColumns().get(0).getName() + "':\n");
        str.append("=@table." + this.getName() + "." + this.getColumns().get(0).getName() + "\n");
        str.append("\n");

        if (this.getColumns().size() > 1) {
            str.append("Get data from two columns:\n");
            str.append("=@table." + this.getName() + "." + this.getColumns().get(0).getName() + "," + this.getColumns().get(1).getName() + "\n");
            str.append("\n");
        }

        str.append("Get data from first row, column '" + this.getColumns().get(0).getName() + "':\n");
        str.append("=@table." + this.getName() + "." + this.getColumns().get(0).getName() + ".1\n");
        str.append("\n");

        str.append("Get data from first columns, rows 1 to 10:\n");
        str.append("=@table." + this.getName() + "." + this.getColumns().get(0).getName() + ".1-10\n");
        str.append("\n");

        str.append("To add a new row, use the 'Variable update' action:\n");
        str.append("    Variable name: @table." + this.getName() + "\n");
        str.append("    Variable name: @table." + this.getName() + ".addrow\n");
        str.append("    Param1: A\\tB\\tC\n");
        str.append("        Use '\\t' to separate values of each column.\n");
        str.append("\n");

        str.append("To update an existing row, use the 'Variable update' action:\n");
        str.append("    Variable name: @table." + this.getName() + "." + this.getColumns().get(0).getName() + ".1\n");
        str.append("    Param1: A\n");
        str.append("        Use '\\t' to separate values of each column.\n");
        str.append("\n");

        return str.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumn> columns) {
        this.columns = columns;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }


}
