/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table.ui;

import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.util.UtilContext;
import net.sf.sketchlet.plugins.varspaces.table.Table;
import net.sf.sketchlet.plugins.varspaces.table.TableColumn;
import net.sf.sketchlet.plugins.varspaces.table.TableVariableSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class TableDataSourcePanel extends JPanel {

    JTabbedPane tabs = new JTabbedPane();
    Vector<TablePanel> tablePanels = new Vector<TablePanel>();
    TableVariableSpace tableDataSource;
    JButton addBtn = new JButton(Language.translate("add table"), UtilContext.getInstance().getImageIconFromResources("resources/add2.gif"));
    JButton deleteBtn = new JButton(Language.translate("delete"), UtilContext.getInstance().getImageIconFromResources("resources/remove.gif"));
    JButton columnsBtn = new JButton(Language.translate("columns..."), UtilContext.getInstance().getImageIconFromResources("resources/columns.png"));
    JButton renameBtn = new JButton(Language.translate("rename..."), UtilContext.getInstance().getImageIconFromResources("resources/text-field.gif"));
    JButton expressionsBtn = new JButton(Language.translate("expressions"));
    final TableDataSourcePanel thisPanel = this;

    public TableDataSourcePanel(TableVariableSpace dataSource) {
        this.setLayout(new BorderLayout());
        this.tableDataSource = dataSource;

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        toolbar.add(addBtn);
        toolbar.add(deleteBtn);
        toolbar.add(columnsBtn);
        toolbar.add(renameBtn);
        toolbar.add(expressionsBtn);

        addBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String name = "NewTable";
                while (true) {
                    name = JOptionPane.showInputDialog(thisPanel, Language.translate("Table name:"), "NewTable");
                    if (name != null) {
                        if (nameExists(name)) {
                            JOptionPane.showMessageDialog(thisPanel, "The table with the name '" + name + "' already exists.\nYou have to enter unique name.");
                            continue;
                        }
                        Table t = new Table();
                        t.name = name;
                        TableColumn tc = t.addNewColumn();
                        tc.name = "Column1";
                        tableDataSource.addTable(t);
                        load();
                        break;
                    } else {
                        break;
                    }
                };
            }
        });

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = tabs.getSelectedIndex();

                if (n >= 0) {
                    try {
                        int opt = JOptionPane.showConfirmDialog(thisPanel,
                                Language.translate("Are you sure you want to delete this table?"),
                                Language.translate("Delete Table"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        if (opt == JOptionPane.YES_OPTION) {
                            tableDataSource.dropTable(n);
                            tabs.remove(n);
                            tablePanels.remove(n);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });

        columnsBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = tabs.getSelectedIndex();

                if (n >= 0) {
                    try {
                        ColumnsDialog dlg = new ColumnsDialog(thisPanel, tableDataSource.tablesVector.elementAt(n));
                        dlg.setVisible(true);
                    } catch (Exception e) {
                    }
                }
            }
        });

        renameBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = tabs.getSelectedIndex();

                if (n >= 0) {
                    try {
                        Table t = tableDataSource.tablesVector.elementAt(n);

                        while (true) {
                            String name = JOptionPane.showInputDialog(thisPanel, Language.translate("Table name:"), t.name);
                            if (name != null) {
                                if (nameExists(name, t)) {
                                    JOptionPane.showMessageDialog(thisPanel, "Another table with the name '" + name + "' already exists.\nYou have to enter unique name.");
                                    continue;
                                }
                                t.name = name;
                                tabs.setTitleAt(n, name);
                                break;
                            } else {
                                break;
                            }
                        };
                    } catch (Exception e) {
                    }
                }
            }
        });
        expressionsBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = tabs.getSelectedIndex();

                if (n >= 0) {
                    Table t = tableDataSource.tablesVector.elementAt(n);
                    JOptionPane.showMessageDialog(TableDataSourcePanel.this, new JTextArea(t.getExpressionExamples()));

                }
            }
        });
        add(toolbar, BorderLayout.NORTH);
        load();

        add(tabs);
    }

    public void load() {
        tabs.removeAll();
        tablePanels.removeAllElements();

        for (Table table : tableDataSource.tablesVector) {
            TablePanel tablePanel = new TablePanel(table);
            this.tabs.addTab(table.name, tablePanel);
            this.tablePanels.add(tablePanel);
        }
    }

    public static void main(String args[]) {
        File dir = new File("c:\\temp\\tables");
        FileUtils.deleteDir(dir);
        Table t = new Table();
        t.name = "person";
        t.columns.add(new TableColumn("name\tString\tJohn"));
        t.columns.add(new TableColumn("address\tString\tUtrecht"));

        t.add("Jacobine\tAmsterdam");
        t.add("Zeljko\tAmsterdam");
        t.add("Jacobine\tAmsterdam");
        t.add("Zeljko\tAmsterdam");
        t.add("Jacobine\tAmsterdam");
        t.add("Zeljko\tAmsterdam");

        //t.onSave(dir);
        t.name += "_2";
        //t.onSave(dir);
        t.name += "_3";
        //t.onSave(dir);
        t.name += "_4";
        //t.onSave(dir);

        TableVariableSpace tds = new TableVariableSpace();
        tds.afterProjectOpening();

        System.out.println(tds.evaluate("table.person"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.name"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.address"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.name,address"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.address,address"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.address,name"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.address,name.1"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.address,name.2"));
        System.out.println();
        System.out.println(tds.evaluate("table.person.address,name.1-2"));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new TableDataSourcePanel(tds));
        frame.pack();
        frame.setVisible(true);
    }

    public boolean nameExists(String name) {
        for (Table t : this.tableDataSource.tablesVector) {
            if (t.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean nameExists(String name, Table skipTable) {
        for (Table t : this.tableDataSource.tables.values()) {
            if (t != skipTable && t.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
