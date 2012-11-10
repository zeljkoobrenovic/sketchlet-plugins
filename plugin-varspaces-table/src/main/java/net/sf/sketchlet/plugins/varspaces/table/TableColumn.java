/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table;

/**
 *
 * @author zobrenovic
 */
public class TableColumn {

    public static final String STRING = "String";
    public static final String NUMBER = "Number";
    public String name = "";
    public String type = TableColumn.STRING;
    public String defaultValue = "";

    public TableColumn() {
    }

    public TableColumn(String strDef) {
        String info[] = strDef.split("\t");

        if (info.length > 0) {
            name = info[0].trim();
        }
        if (info.length > 1) {
            type = info[1].trim();
        }
        if (info.length > 2) {
            defaultValue = info[2].trim();
        }
    }

    public String prepareValue(String value) {
        if (!value.isEmpty() && this.type.equalsIgnoreCase(TableColumn.NUMBER)) {
            try {
                return Double.toString(Double.parseDouble(value));
            } catch (Exception e) {
            }
            return "";
        } else {
            return value;
        }
    }

    public String toXML() {
        String str = "<column name='" + this.name + "' type='" + this.type + "' defaultValue='" + this.defaultValue + "'/>";
        return str;
    }

    public static void main(String args[]) {
        System.out.println(new TableColumn("name\tstring\tJohn").toXML());
    }
}
