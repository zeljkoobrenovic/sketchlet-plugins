/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table.ui;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.plugins.varspaces.table.Table;
import net.sf.sketchlet.plugins.varspaces.table.TableColumn;
import net.sf.sketchlet.plugins.varspaces.table.TableVariableSpace;
import net.sf.sketchlet.util.UtilContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author zobrenovic
 */
public class TableDataSourcePanel extends JPanel {

    private JTabbedPane tabs = new JTabbedPane();
    private java.util.List<TablePanel> tablePanels = new ArrayList<TablePanel>();
    private TableVariableSpace tableDataSource;
    private JButton addBtn = new JButton(Language.translate("add table"), UtilContext.getInstance().getImageIconFromResources("resources/add2.gif"));
    private JButton deleteBtn = new JButton(Language.translate("delete"), UtilContext.getInstance().getImageIconFromResources("resources/remove.gif"));
    private JButton columnsBtn = new JButton(Language.translate("columns..."), UtilContext.getInstance().getImageIconFromResources("resources/columns.png"));
    private JButton renameBtn = new JButton(Language.translate("rename..."), UtilContext.getInstance().getImageIconFromResources("resources/text-field.gif"));
    private JButton expressionsBtn = new JButton(Language.translate("expressions"));
    private final TableDataSourcePanel thisPanel = this;

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
                        t.setName(name);
                        TableColumn tc = t.addNewColumn();
                        tc.setName("Column1");
                        tableDataSource.addTable(t);
                        load();
                        break;
                    } else {
                        break;
                    }
                }
                ;
            }
        });

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = getTabs().getSelectedIndex();

                if (n >= 0) {
                    try {
                        int opt = JOptionPane.showConfirmDialog(thisPanel,
                                Language.translate("Are you sure you want to delete this table?"),
                                Language.translate("Delete Table"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        if (opt == JOptionPane.YES_OPTION) {
                            tableDataSource.dropTable(n);
                            getTabs().remove(n);
                            getTablePanels().remove(n);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });

        columnsBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = getTabs().getSelectedIndex();

                if (n >= 0) {
                    try {
                        ColumnsDialog dlg = new ColumnsDialog(thisPanel, tableDataSource.getTablesVector().get(n));
                        dlg.setVisible(true);
                    } catch (Exception e) {
                    }
                }
            }
        });

        renameBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = getTabs().getSelectedIndex();

                if (n >= 0) {
                    try {
                        Table t = tableDataSource.getTablesVector().get(n);

                        while (true) {
                            String name = JOptionPane.showInputDialog(thisPanel, Language.translate("Table name:"), t.getName());
                            if (name != null) {
                                if (nameExists(name, t)) {
                                    JOptionPane.showMessageDialog(thisPanel, "Another table with the name '" + name + "' already exists.\nYou have to enter unique name.");
                                    continue;
                                }
                                t.setName(name);
                                getTabs().setTitleAt(n, name);
                                break;
                            } else {
                                break;
                            }
                        }
                        ;
                    } catch (Exception e) {
                    }
                }
            }
        });

        expressionsBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = getTabs().getSelectedIndex();

                if (n >= 0) {
                    Table t = tableDataSource.getTablesVector().get(n);
                    JOptionPane.showMessageDialog(TableDataSourcePanel.this, new JTextArea(t.getExpressionExamples()));

                }
            }
        });

        add(toolbar, BorderLayout.NORTH);
        load();

        add(getTabs());
    }

    private void load() {
        getTabs().removeAll();
        getTablePanels().clear();

        for (Table table : tableDataSource.getTablesVector()) {
            TablePanel tablePanel = new TablePanel(table);
            this.getTabs().addTab(table.getName(), tablePanel);
            this.getTablePanels().add(tablePanel);
        }
    }

    private boolean nameExists(String name) {
        for (Table t : this.tableDataSource.getTablesVector()) {
            if (t.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean nameExists(String name, Table skipTable) {
        for (Table t : this.tableDataSource.getTables().values()) {
            if (t != skipTable && t.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public JTabbedPane getTabs() {
        return tabs;
    }

    public void setTabs(JTabbedPane tabs) {
        this.tabs = tabs;
    }

    public java.util.List<TablePanel> getTablePanels() {
        return tablePanels;
    }

    public void setTablePanels(java.util.List<TablePanel> tablePanels) {
        this.tablePanels = tablePanels;
    }
}
