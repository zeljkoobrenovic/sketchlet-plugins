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
    private String name = "";
    private String type = TableColumn.STRING;
    private String defaultValue = "";

    public TableColumn() {
    }

    public TableColumn(String strDef) {
        String info[] = strDef.split("\t");

        if (info.length > 0) {
            setName(info[0].trim());
        }
        if (info.length > 1) {
            setType(info[1].trim());
        }
        if (info.length > 2) {
            setDefaultValue(info[2].trim());
        }
    }

    public String prepareValue(String value) {
        if (!value.isEmpty() && this.getType().equalsIgnoreCase(TableColumn.NUMBER)) {
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
        String str = "<column name='" + this.getName() + "' type='" + this.getType() + "' defaultValue='" + this.getDefaultValue() + "'/>";
        return str;
    }

    public static void main(String args[]) {
        System.out.println(new TableColumn("name\tstring\tJohn").toXML());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
