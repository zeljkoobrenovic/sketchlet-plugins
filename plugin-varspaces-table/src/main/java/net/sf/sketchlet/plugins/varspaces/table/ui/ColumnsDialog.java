/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table.ui;

import net.sf.sketchlet.plugins.varspaces.table.Table;
import net.sf.sketchlet.plugins.varspaces.table.TableColumn;
import net.sf.sketchlet.common.translation.Language;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import net.sf.sketchlet.util.UtilContext;

/**
 *
 * @author zobrenovic
 */
public class ColumnsDialog extends JDialog {

    static String columns[] = new String[]{"Name", "Type", "Default Value"};
    JTable jTable;
    Table table;
    ColumnsTableModel model;
    JButton closeBtn = new JButton(Language.translate("Close"), UtilContext.getInstance().getImageIconFromResources("resources/ok.png"));
    JButton deleteBtn = new JButton(Language.translate("delete"), UtilContext.getInstance().getImageIconFromResources("resources/remove.gif"));
    JButton upBtn = new JButton(Language.translate("move up"), UtilContext.getInstance().getImageIconFromResources("resources/go-up.png"));
    JButton downBtn = new JButton(Language.translate("move down"), UtilContext.getInstance().getImageIconFromResources("resources/go-down.png"));
    TableDataSourcePanel parentPanel;

    public ColumnsDialog(final TableDataSourcePanel parentPanel, final Table table) {
        this.setModal(false);
        this.setTitle(Language.translate("Columns"));
        this.setLayout(new BorderLayout());
        this.table = table;
        this.parentPanel = parentPanel;

        createGUI();
    }

    public void createGUI() {
        this.model = new ColumnsTableModel();
        this.jTable = new JTable(model);

        JToolBar buttons = new JToolBar();
        buttons.setFloatable(false);
        buttons.add(this.closeBtn);

        JPanel buttonsNorth = new JPanel();
        buttonsNorth.add(this.deleteBtn);
        buttonsNorth.add(this.upBtn);
        buttonsNorth.add(this.downBtn);

        add(new JScrollPane(jTable));
        add(buttons, BorderLayout.SOUTH);
        add(buttonsNorth, BorderLayout.NORTH);
        setSize(400, 400);

        closeBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = jTable.getSelectedRow();
                int tn = parentPanel.tabs.getSelectedIndex();
                if (n >= 0 && tn >= 0) {
                    table.dropColumn(n);
                    model.fireTableStructureChanged();
                    model.fireTableDataChanged();
                    parentPanel.tablePanels.elementAt(tn).model.fireTableStructureChanged();
                    parentPanel.tablePanels.elementAt(tn).model.fireTableDataChanged();
                }
            }
        });
        upBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = jTable.getSelectedRow();
                int tn = parentPanel.tabs.getSelectedIndex();
                if (n > 0 && tn >= 0) {
                    table.replaceColumns(n, n - 1);
                    model.fireTableStructureChanged();
                    model.fireTableDataChanged();
                    parentPanel.tablePanels.elementAt(tn).model.fireTableStructureChanged();
                    parentPanel.tablePanels.elementAt(tn).model.fireTableDataChanged();
                    jTable.getSelectionModel().setSelectionInterval(n - 1, n - 1);
                }
            }
        });
        downBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = jTable.getSelectedRow();
                int tn = parentPanel.tabs.getSelectedIndex();
                if (n >= 0 && n < table.columns.size() - 1 && tn >= 0) {
                    table.replaceColumns(n, n + 1);
                    model.fireTableStructureChanged();
                    model.fireTableDataChanged();
                    parentPanel.tablePanels.elementAt(tn).model.fireTableStructureChanged();
                    parentPanel.tablePanels.elementAt(tn).model.fireTableDataChanged();
                    jTable.getSelectionModel().setSelectionInterval(n + 1, n + 1);
                }
            }
        });
        jTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                enableControls();
            }
        });
        enableControls();
    }

    public void enableControls() {
        int n = this.jTable.getSelectedRow();
        this.deleteBtn.setEnabled(n >= 0);
        this.upBtn.setEnabled(n > 0);
        this.downBtn.setEnabled(n >= 0 && n < this.table.columns.size() - 1);
    }

    class ColumnsTableModel extends AbstractTableModel {

        public String getColumnName(int col) {
            return columns[col];
        }

        public int getRowCount() {
            return table.columns.size() + 1;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int col) {
            if (row >= table.columns.size()) {
                return "";
            } else {
                TableColumn column = table.columns.elementAt(row);
                switch (col) {
                    case 0:
                        return column.name;
                    case 1:
                        return column.type;
                    case 2:
                        return column.defaultValue;
                    default:
                        return "";
                }
            }

        }

        public boolean isCellEditable(int row, int col) {
            return col != 1;
        }

        public void setValueAt(Object value, int row, int col) {
            if (row == table.columns.size()) {
                table.addNewColumn();
            }
            TableColumn column = table.columns.elementAt(row);
            switch (col) {
                case 0:
                    column.name = value.toString();
                    break;
                case 1:
                    column.type = value.toString();
                    break;
                case 2:
                    column.defaultValue = value.toString();
                    break;
            }
            model.fireTableStructureChanged();
            model.fireTableDataChanged();
            int tn = parentPanel.tabs.getSelectedIndex();
            parentPanel.tablePanels.elementAt(tn).model.fireTableStructureChanged();
            parentPanel.tablePanels.elementAt(tn).model.fireTableDataChanged();
        }

        public Class getColumnClass(int c) {
            return String.class;
        }
    }
}
