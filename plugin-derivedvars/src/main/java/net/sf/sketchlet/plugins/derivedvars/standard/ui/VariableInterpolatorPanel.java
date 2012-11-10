/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.plugins.derivedvars.standard.ui;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.VariableInterpolator;
import net.sf.sketchlet.util.ui.DataRowFrame;
import net.sf.sketchlet.common.translation.Language;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
public class VariableInterpolatorPanel extends JPanel {

    JTable tableInterpolator;
    AbstractTableModel interpolatorModel;
    VariableInterpolator interpolator;
    JComboBox varCombo = new JComboBox();

    public VariableInterpolatorPanel(VariableInterpolator interpolator) {
        this.interpolator = interpolator;
        setLayout(new BorderLayout());
        VariablesBlackboardContext.getInstance().populateVariablesCombo(varCombo, false);

        interpolatorModel = interpolator.getTableModel();
        tableInterpolator = new JTable(interpolatorModel);
        tableInterpolator.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tableInterpolator.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(varCombo));
        tableInterpolator.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(varCombo));

        JScrollPane scrollPaneInterpolator = new JScrollPane(tableInterpolator);
        this.add(scrollPaneInterpolator, BorderLayout.CENTER);
        this.add(getInterpolatorButtons(), BorderLayout.EAST);

        setDragAndDropHandlers();


    }

    public void setDragAndDropHandlers() {
        new FileDrop(System.out, tableInterpolator, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (strText.startsWith("=") && strText.length() > 1) {
                    for (int i = 0; i < interpolator.data.length; i++) {
                        if (interpolator.data[i][0].toString().isEmpty()) {
                            interpolator.data[i][0] = strText.substring(1);
                            editInterpolatorRow(i);
                            break;
                        }
                    }
                }
                DataRowFrame.emptyOnCancel = false;
            }
        });
    }

    public void save() {
        interpolator.onSave();
    }

    public void load() {
        interpolator.load();
    }
    JComboBox comboBoxInterpolator = new JComboBox();

    public JPanel getInterpolatorButtons() {
        comboBoxInterpolator = new JComboBox();
        comboBoxInterpolator.setEditable(true);
        comboBoxInterpolator.addItem("");

        comboBoxInterpolator.addItem("0");
        comboBoxInterpolator.addItem("00");
        comboBoxInterpolator.addItem("000");
        comboBoxInterpolator.addItem("0.00");

        tableInterpolator.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    editInterpolatorRow();
                }
            }
        });
        JPanel interpolatorButtons = new JPanel(new GridLayout(0, 1));
        final JButton deleteBtn = new JButton(Language.translate("Delete"));
        final JButton duplicateBtn = new JButton(Language.translate("Duplicate"));
        final JButton moveUpBtn = new JButton(Language.translate("Move Up"));
        final JButton moveDownBtn = new JButton(Language.translate("Move Down"));
        final JButton editBtn = new JButton(Language.translate("Edit"));

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableInterpolator.getSelectedRow();
                if (row >= 0) {
                    int r = row;

                    for (int i = row; i < interpolator.data.length - 1; i++) {
                        interpolator.data[i] = interpolator.data[i + 1];
                    }

                    interpolator.data[interpolator.data.length - 1] = new Object[]{"", "", "", "", "", "", "", ""};
                    interpolatorModel.fireTableDataChanged();
                    tableInterpolator.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        duplicateBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableInterpolator.getSelectedRow();
                if (row < interpolator.data.length - 1) {
                    for (int i = interpolator.data.length - 2; i >= row + 1; i--) {
                        interpolator.data[i + 1] = interpolator.data[i];
                    }

                    interpolator.data[row + 1] = new Object[]{
                        "" + interpolator.data[row][0],
                        "" + interpolator.data[row][1],
                        "" + interpolator.data[row][2],
                        "" + interpolator.data[row][3],
                        "" + interpolator.data[row][4],
                        "" + interpolator.data[row][5],
                        "" + interpolator.data[row][6],
                        "" + interpolator.data[row][7]};

                    int r = row + 1;

                    interpolatorModel.fireTableDataChanged();
                    tableInterpolator.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveUpBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableInterpolator.getSelectedRow();
                if (row > 0) {
                    Object[] rowData1 = interpolator.data[row];
                    Object[] rowData2 = interpolator.data[row - 1];

                    interpolator.data[row] = rowData2;
                    interpolator.data[row - 1] = rowData1;

                    int r = row - 1;

                    interpolatorModel.fireTableDataChanged();
                    tableInterpolator.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDownBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableInterpolator.getSelectedRow();
                if (row < interpolator.data.length - 1) {
                    Object[] rowData1 = interpolator.data[row];
                    Object[] rowData2 = interpolator.data[row + 1];

                    interpolator.data[row] = rowData2;
                    interpolator.data[row + 1] = rowData1;

                    int r = row + 1;

                    interpolatorModel.fireTableDataChanged();
                    tableInterpolator.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        editBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                editInterpolatorRow();
            }
        });

        interpolatorButtons.add(deleteBtn);
        interpolatorButtons.add(duplicateBtn);
        interpolatorButtons.add(moveUpBtn);
        interpolatorButtons.add(moveDownBtn);
        interpolatorButtons.add(editBtn);

        tableInterpolator.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableInterpolator.getSelectedRow();
                deleteBtn.setEnabled(row >= 0);
                editBtn.setEnabled(row >= 0);
                moveUpBtn.setEnabled(row > 0);
                duplicateBtn.setEnabled(row < VariableInterpolator.interpolator.data.length - 1);
                moveDownBtn.setEnabled(row < VariableInterpolator.interpolator.data.length - 1);
            }
        });
        tableInterpolator.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBoxInterpolator));
        tableInterpolator.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(comboBoxInterpolator));

        JPanel wrapper = new JPanel();
        wrapper.add(interpolatorButtons);
        return wrapper;
    }

    public void editInterpolatorFreeRow(String strVariable) {
        for (int i = 0; i < interpolatorModel.getRowCount(); i++) {
            if (interpolatorModel.getValueAt(i, 0).toString().isEmpty()) {
                interpolatorModel.setValueAt(strVariable, i, 0);
                editInterpolatorRow(i);
                break;
            }
        }
    }

    public void editInterpolatorRow() {
        int row = tableInterpolator.getSelectedRow();
        editInterpolatorRow(row);
    }

    public void editInterpolatorRow(int row) {
        if (row >= 0) {
            Object editors[] = new Object[VariableInterpolator.interpolator.columnNames.length];
            editors[0] = DataRowFrame.cloneComboBox(varCombo);
            editors[4] = DataRowFrame.cloneComboBox(varCombo);
            editors[3] = DataRowFrame.cloneComboBox(comboBoxInterpolator);
            editors[7] = DataRowFrame.cloneComboBox(comboBoxInterpolator);
            new DataRowFrame(SketchletContext.getInstance().getEditorFrame(),
                    Language.translate("Numeric Mapping"),
                    row,
                    VariableInterpolator.interpolator.columnNames,
                    editors, null, null,
                    tableInterpolator,
                    interpolatorModel);
        }
    }
    static JComboBox lastComboBoxParam1 = null;
}
