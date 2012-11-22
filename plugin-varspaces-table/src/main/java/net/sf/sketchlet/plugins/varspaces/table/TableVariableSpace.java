/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.plugin.AbstractPlugin;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.VariableSpacePlugin;
import net.sf.sketchlet.plugins.varspaces.table.ui.TableDataSourcePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "table", type = "varspace")
public class TableVariableSpace extends AbstractPlugin implements VariableSpacePlugin {

    private Map<String, Table> tables = new HashMap<String, Table>();
    private List<Table> tablesVector = new ArrayList<Table>();

    @Override
    public void afterProjectOpening() {
        getTables().clear();
        getTablesVector().clear();

        File file = this.getFile();
        if (file.exists()) {
            TablesSaxLoader.loadTable(file, this);
        }
    }

    @Override
    public void afterApplicationStart() {
    }

    @Override
    public void beforeApplicationEnd() {
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    public void addTable(Table table) {
        getTablesVector().add(table);
        getTables().put(table.getName(), table);
    }

    public File getFile() {
        File dir = new File(SketchletContext.getInstance().getCurrentProjectDirectory() + ".amico/data/");
        dir.mkdirs();
        return new File(dir, "tables.xml");
    }

    @Override
    public void beforeProjectClosing() {
        onSave();
    }

    @Override
    public void onSave() {
        try {
            File file = this.getFile();
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<tables>");

            for (Table t : getTablesVector()) {
                t.save(out);
            }

            out.println("</tables>");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dropTable(int index) {
        this.dropTable(this.getTablesVector().get(index));
    }

    public void dropTable(Table table) {
        this.getTables().remove(table.getName());
        this.getTablesVector().remove(table);
    }

    // @table.person.name,address.1
    public Info getInfo(String id) {
        Info info = null;
        try {
            String el[] = id.split("\\.");

            String tableName = el.length > 0 ? el[0] : null;
            String columns = el.length > 1 ? el[1] : null;
            String rows = el.length > 2 ? el[2] : null;

            if (tableName != null) {
                info = new Info();
                info.table = getTables().get(tableName);
                if (info.table != null && columns != null) {
                    if (columns.equalsIgnoreCase("*")) {
                        info.col = new int[info.table.getColumns().size()];

                        for (int i = 0; i < info.col.length; i++) {
                            info.col[i] = i;
                        }
                    } else {
                        String cols[] = columns.split(",");
                        info.col = new int[cols.length];

                        for (int i = 0; i < info.col.length; i++) {
                            String colName = cols[i].trim();
                            info.col[i] = -1;
                            for (int j = 0; j < info.table.getColumns().size(); j++) {
                                TableColumn tc = info.table.getColumns().get(j);
                                if (tc.getName().equalsIgnoreCase(colName)) {
                                    info.col[i] = j;
                                    break;
                                }
                            }
                        }
                    }

                    info.row1 = -1;
                    info.row2 = -1;

                    if (rows != null && !columns.equalsIgnoreCase("addrow")) {
                        String rowIndex[] = rows.split("-");
                        if (!rowIndex[0].isEmpty()) {
                            info.row1 = (int) Double.parseDouble(rowIndex[0]) - 1;
                        }
                        if (rowIndex.length > 1 && !rowIndex[1].isEmpty()) {
                            info.row2 = (int) Double.parseDouble(rowIndex[1]) - 1;
                        } else {
                            info.row2 = info.row1;
                        }
                    }
                }

                return info;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void update(String id, String value) {
        Info info = this.getInfo(id);
        this.update(info, value);
    }

    private void update(Info info, String value) {
        value = value.replace("\\t", "\t");
        value = value.replace("\\n", "\t");

        if (info != null && info.table != null) {
            if (info.col != null && info.col.length > 0) {
                if (info.row1 == -1) {
                    info.table.add(value.split("\t"), info.col);
                } else {
                    String strRow[] = value.split("\n");
                    for (int r = 0; r < strRow.length; r++) {
                        info.table.update(info.row1 + r, value.split("\t"), info.col);
                    }
                }
            } else {
                info.table.add(value);
            }
        }
    }

    @Override
    public String evaluate(String id) {
        Info info = this.getInfo(id);

        if (info != null && info.table != null) {
            if (info.col != null && info.col.length > 0) {
                StringBuffer str = new StringBuffer("");
                int r = 0;
                for (List<String> row : info.table.getData()) {
                    if ((info.row1 == -1 || info.row1 <= r) && (info.row2 == -1 || info.row2 >= r)) {
                        int i = 0;
                        for (int c : info.col) {
                            if (i > 0) {
                                str.append("\t");
                            }
                            if (c >= 0) {
                                i++;
                                str.append(row.get(c));
                            }
                        }
                        str.append("\n");
                    }
                    r++;
                }

                return str.toString();
            } else {
                return info.table.toString();
            }
        }

        return "";
    }

    @Override
    public void delete(String id) {
    }

    @Override
    public Component getGUI() {
        return new TableDataSourcePanel(this);
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public void setTables(Map<String, Table> tables) {
        this.tables = tables;
    }

    public List<Table> getTablesVector() {
        return tablesVector;
    }

    public void setTablesVector(List<Table> tablesVector) {
        this.tablesVector = tablesVector;
    }

    public class Info {

        private Table table;
        private int col[] = null;
        private int row1 = -1;
        private int row2 = -1;

        public String toString() {
            String strInfo = "table: " + (table != null ? table.getName() : "NULL") + ", ";
            strInfo += "col={";
            if (this.col != null) {
                for (int i = 0; i < col.length; i++) {
                    if (i > 0) {
                        strInfo += ",";
                    }
                    strInfo += col[i];
                }
            } else {
                strInfo += "NULL";
            }
            strInfo += "}, ";
            strInfo += "row1: " + this.row1 + ", row2: " + this.row2;
            return strInfo;
        }
    }
}
