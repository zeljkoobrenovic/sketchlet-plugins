/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.varspaces.table.ui;

import net.sf.sketchlet.plugins.varspaces.table.Table;
import net.sf.sketchlet.plugins.varspaces.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author zobrenovic
 */
public class TablePanel extends JPanel {

    Table table;
    JTable jTable;
    JButton deleteBtn = new JButton("delete row");
    JButton pasteBtn = new JButton("paste from clipboard");
    JButton copyBtn = new JButton("copy to clipboard");
    TableTableModel model;
    int row = -1;

    public TablePanel(Table table) {
        this.setLayout(new BorderLayout());
        this.table = table;
        createGUI();
    }

    public void createGUI() {
        // creating the table
        this.model = new TableTableModel();
        this.jTable = new JTable(this.model);
        jTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                row = jTable.getSelectedRow();
                enableControls();
            }
        });

        // creating the buttons panel
        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int rows[] = jTable.getSelectedRows();
                for (int i = rows.length - 1; i >= 0; i--) {
                    if (rows[i] < table.data.size()) {
                        table.data.removeElementAt(rows[i]);
                    }
                }
                model.fireTableDataChanged();
            }
        });
        pasteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable contents = clipboard.getContents(null);
                    if ((contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        if (table.data.size() > 0) {
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
                                table.data.removeAllElements();
                            }
                        }

                        String strData = (String) contents.getTransferData(DataFlavor.stringFlavor);
                        int colCount = getColumnCount(strData);

                        if (colCount > table.columns.size()) {
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
                                for (int i = table.columns.size(); i < colCount; i++) {
                                    TableColumn tc = new TableColumn();
                                    tc.name = table.getUniqueColumnName();
                                    table.addColumn(tc);
                                    model.fireTableStructureChanged();
                                }
                            }
                        }

                        table.addRows(strData);
                        model.fireTableDataChanged();
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

        // add to the main panel
        this.add(new JScrollPane(this.jTable), BorderLayout.CENTER);
        this.add(buttons, BorderLayout.SOUTH);

        enableControls();
    }

    public int getColumnCount(String data) {
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

    public void enableControls() {
        deleteBtn.setEnabled(row >= 0);
    }

    class TableTableModel extends AbstractTableModel {

        public String getColumnName(int col) {
            return table.columns.elementAt(col).name;
        }

        public int getRowCount() {
            return table.data.size() + 1;
        }

        public int getColumnCount() {
            return table.columns.size();
        }

        public Object getValueAt(int row, int col) {
            if (row >= 0 && row < table.data.size() && col >= 0 && col < table.data.elementAt(row).size()) {
                return table.data.elementAt(row).elementAt(col);
            } else {
                return "";
            }
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            if (row == table.data.size()) {
                table.addEmptyRow();
            }
            table.data.elementAt(row).setElementAt((String) value, col);
            model.fireTableDataChanged();
        }

        public Class getColumnClass(int c) {
            return String.class;
        }
    }
}
