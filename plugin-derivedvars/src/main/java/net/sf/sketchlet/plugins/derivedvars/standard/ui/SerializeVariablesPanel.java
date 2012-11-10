/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.plugins.derivedvars.standard.ui;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.SerializeVariables;
import net.sf.sketchlet.util.ui.DataRowFrame;
import net.sf.sketchlet.common.translation.Language;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
public class SerializeVariablesPanel extends JPanel {

    JTable tableSerialize;
    AbstractTableModel serializeModel;
    SerializeVariables serializeVariables;
    JComboBox varCombo = new JComboBox();

    public SerializeVariablesPanel(SerializeVariables serializeVariables) {
        this.serializeVariables = serializeVariables;
        setLayout(new BorderLayout());
        VariablesBlackboardContext.getInstance().populateVariablesCombo(varCombo, false);

        serializeModel = serializeVariables.getTableModel();
        tableSerialize = new JTable(serializeModel);
        tableSerialize.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableSerialize.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(varCombo));

        JScrollPane scrollPaneSerialize = new JScrollPane(tableSerialize);
        this.add(scrollPaneSerialize, BorderLayout.CENTER);
        this.add(getSerializeButtons(), BorderLayout.EAST);

        setDragAndDropHandlers();
    }

    public void setDragAndDropHandlers() {
        new FileDrop(System.out, tableSerialize, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (strText.startsWith("=") && strText.length() > 1) {
                    for (int i = 0; i < serializeVariables.data.length; i++) {
                        if (serializeVariables.data[i][0].toString().isEmpty()) {
                            serializeVariables.data[i][0] = strText.substring(1);
                            editSerializeRow(i);
                            break;
                        }
                    }
                }
                DataRowFrame.emptyOnCancel = false;
            }
        });
    }

    public void save() {
        serializeVariables.onSave();
    }

    public void load() {
        serializeVariables.load();
    }

    public JPanel getSerializeButtons() {
        JPanel serializeButtons = new JPanel(new GridLayout(0, 1));
        final JButton deleteBtn = new JButton(Language.translate("Delete"));
        final JButton duplicateBtn = new JButton(Language.translate("Duplicate"));
        final JButton moveUpBtn = new JButton(Language.translate("Move Up"));
        final JButton moveDownBtn = new JButton(Language.translate("Move Down"));
        final JButton editBtn = new JButton(Language.translate("Edit"));

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableSerialize.getSelectedRow();
                if (row >= 0) {
                    int r = row;

                    for (int i = row; i < serializeVariables.data.length - 1; i++) {
                        serializeVariables.data[i] = serializeVariables.data[i + 1];
                    }

                    serializeVariables.data[serializeVariables.data.length - 1] = new Object[]{"", "", "", "", "", new Boolean(false)};

                    serializeModel.fireTableDataChanged();

                    tableSerialize.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        duplicateBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableSerialize.getSelectedRow();
                if (row < serializeVariables.data.length - 1) {
                    for (int i = serializeVariables.data.length - 2; i >= row + 1; i--) {
                        serializeVariables.data[i + 1] = serializeVariables.data[i];
                    }

                    serializeVariables.data[row + 1] = new Object[]{
                        "" + serializeVariables.data[row][0],
                        "" + serializeVariables.data[row][1],
                        "" + serializeVariables.data[row][2],
                        "" + serializeVariables.data[row][3],
                        "" + serializeVariables.data[row][4],
                        new Boolean((Boolean) serializeVariables.data[row][5])};

                    int r = row + 1;

                    serializeModel.fireTableDataChanged();
                    tableSerialize.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveUpBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableSerialize.getSelectedRow();
                if (row > 0) {
                    Object[] rowData1 = serializeVariables.data[row];
                    Object[] rowData2 = serializeVariables.data[row - 1];

                    serializeVariables.data[row] = rowData2;
                    serializeVariables.data[row - 1] = rowData1;

                    int r = row - 1;

                    serializeModel.fireTableDataChanged();

                    tableSerialize.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDownBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableSerialize.getSelectedRow();
                if (row < serializeVariables.data.length - 1) {
                    Object[] rowData1 = serializeVariables.data[row];
                    Object[] rowData2 = serializeVariables.data[row + 1];

                    serializeVariables.data[row] = rowData2;
                    serializeVariables.data[row + 1] = rowData1;

                    int r = row + 1;

                    serializeModel.fireTableDataChanged();

                    tableSerialize.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        tableSerialize.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableSerialize.getSelectedRow();
                deleteBtn.setEnabled(row >= 0);
                editBtn.setEnabled(row >= 0);
                moveUpBtn.setEnabled(row > 0);
                duplicateBtn.setEnabled(row < serializeVariables.data.length - 1);
                moveDownBtn.setEnabled(row < serializeVariables.data.length - 1);
            }
        });
        serializeButtons.add(deleteBtn);
        serializeButtons.add(duplicateBtn);
        serializeButtons.add(moveUpBtn);
        serializeButtons.add(moveDownBtn);
        serializeButtons.add(editBtn);

        editBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableSerialize.getSelectedRow();
                editSerializeRow(row);
            }
        });

        JPanel wrapper = new JPanel();
        wrapper.add(serializeButtons);
        return wrapper;
    }

    public void editSerializeFreeRow(String strVariable) {
        for (int i = 0; i < serializeModel.getRowCount(); i++) {
            if (serializeModel.getValueAt(i, 0).toString().isEmpty()) {
                serializeModel.setValueAt(strVariable, i, 0);
                editSerializeRow(i);
                break;
            }
        }
    }

    public void editSerializeRow(int row) {
        if (row >= 0) {
            Object editors[] = new Object[SerializeVariables.serializeVariables.columnNames.length];
            editors[0] = DataRowFrame.cloneComboBox(varCombo);
            new DataRowFrame(SketchletContext.getInstance().getEditorFrame(),
                    Language.translate("Serialize"),
                    row,
                    SerializeVariables.serializeVariables.columnNames,
                    editors, null, null,
                    tableSerialize,
                    serializeModel);
        }
    }
}
