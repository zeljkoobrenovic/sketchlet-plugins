/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table.ui;

import net.sf.sketchlet.plugins.varspaces.table.Table;
import net.sf.sketchlet.plugins.varspaces.table.TableColumn;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author zobrenovic
 */
public class TablePanel extends JPanel {

    private Table table;
    private JTable tableComponent;
    private JButton deleteBtn = new JButton("delete row");
    private JButton pasteBtn = new JButton("paste from clipboard");
    private JButton copyBtn = new JButton("copy to clipboard");
    private TableTableModel tableModel;
    private int row = -1;

    public TablePanel(Table table) {
        this.setLayout(new BorderLayout());
        this.table = table;
        createGUI();
    }

    private void createGUI() {
        this.setTableModel(new TableTableModel());
        this.tableComponent = new JTable(this.getTableModel());
        tableComponent.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                row = tableComponent.getSelectedRow();
                enableControls();
            }
        });

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int rows[] = tableComponent.getSelectedRows();
                for (int i = rows.length - 1; i >= 0; i--) {
                    if (rows[i] < table.getData().size()) {
                        table.getData().remove(rows[i]);
                    }
                }
                getTableModel().fireTableDataChanged();
            }
        });

        pasteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable contents = clipboard.getContents(null);
                    if ((contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        if (table.getData().size() > 0) {
                            Object[] options = {"Append", "Replace", "Cancel"};
                            int n = JOptionPane.showOptionDialog(null,
                                    "Do you want to append the clipboard data to existing data\n"
                                            + "or do you want to replace existing data?",
                                    "Paste Data",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null, //do not use a custom Icon
                                    options, //the titles of buttons
                                    options[0]); //default button title
                            if (n == JOptionPane.CANCEL_OPTION) {
                                return;
                            }
                            if (n == JOptionPane.NO_OPTION) {
                                table.getData().clear();
                            }
                        }

                        String strData = (String) contents.getTransferData(DataFlavor.stringFlavor);
                        int colCount = getColumnCount(strData);

                        if (colCount > table.getColumns().size()) {
                            Object[] options = {"Add New Columns", "Don't Add", "Cancel"};
                            int n = JOptionPane.showOptionDialog(null,
                                    "Data on the clipboard have more columns than the table.\n"
                                            + "Do you want to add new columns to your table??",
                                    "New Columns",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null, //do not use a custom Icon
                                    options, //the titles of buttons
                                    options[0]); //default button title
                            if (n == JOptionPane.CANCEL_OPTION) {
                                return;
                            }
                            if (n == JOptionPane.YES_OPTION) {
                                for (int i = table.getColumns().size(); i < colCount; i++) {
                                    TableColumn tc = new TableColumn();
                                    tc.setName(table.getUniqueColumnName());
                                    table.addColumn(tc);
                                    getTableModel().fireTableStructureChanged();
                                }
                            }
                        }

                        table.addRows(strData);
                        getTableModel().fireTableDataChanged();
                    }
                } catch (UnsupportedFlavorException ex) {
                } catch (IOException ex) {
                }
            }
        });

        copyBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                StringSelection stringSelection = new StringSelection(table.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        JPanel buttons = new JPanel();
        buttons.add(this.deleteBtn);
        buttons.add(this.pasteBtn);
        buttons.add(this.copyBtn);

        this.add(new JScrollPane(this.tableComponent), BorderLayout.CENTER);
        this.add(buttons, BorderLayout.SOUTH);

        enableControls();
    }

    private int getColumnCount(String data) {
        int cols = 0;
        String rows[] = data.split("\n");
        for (String row : rows) {
            int n = row.split("\t").length;
            if (n > cols) {
                cols = n;
            }
        }

        return cols;
    }

    private void enableControls() {
        deleteBtn.setEnabled(row >= 0);
    }

    public TableTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableTableModel tableModel) {
        this.tableModel = tableModel;
    }

    class TableTableModel extends AbstractTableModel {

        public String getColumnName(int col) {
            return table.getColumns().get(col).getName();
        }

        public int getRowCount() {
            return table.getData().size() + 1;
        }

        public int getColumnCount() {
            return table.getColumns().size();
        }

        public Object getValueAt(int row, int col) {
            if (row >= 0 && row < table.getData().size() && col >= 0 && col < table.getData().get(row).size()) {
                return table.getData().get(row).get(col);
            } else {
                return "";
            }
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            if (row == table.getData().size()) {
                table.addEmptyRow();
            }
            table.getData().get(row).set(col, (String) value);
            getTableModel().fireTableDataChanged();
        }

        public Class getColumnClass(int c) {
            return String.class;
        }
    }
}
