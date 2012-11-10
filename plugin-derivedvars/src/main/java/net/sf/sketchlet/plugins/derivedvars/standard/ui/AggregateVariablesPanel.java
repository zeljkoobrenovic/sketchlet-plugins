/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.derivedvars.standard.ui;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.AggregateVariables;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.util.ui.DataRowFrame;
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
public class AggregateVariablesPanel extends JPanel {

    JTable tableAggregate;
    AbstractTableModel aggregateModel;
    AggregateVariables aggregateVariables;
    JComboBox varCombo = new JComboBox();
    JComboBox comboBoxAggregate = new JComboBox();
    JComboBox comboBoxFormatAggregate = new JComboBox();

    public AggregateVariablesPanel(AggregateVariables aggregateVariables) {
        this.aggregateVariables = aggregateVariables;
        VariablesBlackboardContext.getInstance().populateVariablesCombo(varCombo, false);

        this.setLayout(new BorderLayout());
        aggregateModel = aggregateVariables.getTableModel();
        tableAggregate = new JTable(aggregateModel);
        tableAggregate.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableAggregate.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(varCombo));

        JScrollPane scrollPaneAggregate = new JScrollPane(tableAggregate);
        this.add(scrollPaneAggregate, BorderLayout.CENTER);
        this.add(getAggregateButtons(), BorderLayout.EAST);

        setDragAndDropHandlers();
    }

    public void setDragAndDropHandlers() {
        new FileDrop(System.out, tableAggregate, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (strText.startsWith("=") && strText.length() > 1) {
                    for (int i = 0; i < aggregateVariables.data.length; i++) {
                        if (aggregateVariables.data[i][0].toString().isEmpty()) {
                            aggregateVariables.data[i][0] = strText.substring(1);
                            editAggregateVar(i);
                            break;
                        }
                    }
                }
                DataRowFrame.emptyOnCancel = false;
            }
        });
    }

    JComboBox comboBoxDiscrete = new JComboBox();
    JComboBox operatorsDiscrete = new JComboBox();

    static JComboBox lastComboBoxParam1 = null;

    public JPanel getAggregateButtons() {
        JPanel aggregateButtons = new JPanel(new GridLayout(0, 1));
        final JButton deleteBtn = new JButton(Language.translate("Delete"));
        final JButton duplicateBtn = new JButton(Language.translate("Duplicate"));
        final JButton moveUpBtn = new JButton(Language.translate("Move Up"));
        final JButton moveDownBtn = new JButton(Language.translate("Move Down"));
        final JButton editBtn = new JButton(Language.translate("Edit"));

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAggregate.getSelectedRow();
                if (row >= 0) {
                    int r = row;

                    for (int i = row; i < aggregateVariables.data.length - 1; i++) {
                        aggregateVariables.data[i] = aggregateVariables.data[i + 1];
                    }

                    aggregateVariables.data[aggregateVariables.data.length - 1] = new Object[]{"", "", "", "", ""};
                    aggregateModel.fireTableDataChanged();
                    tableAggregate.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        duplicateBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAggregate.getSelectedRow();
                if (row < aggregateVariables.data.length - 1) {
                    for (int i = aggregateVariables.data.length - 2; i >= row + 1; i--) {
                        aggregateVariables.data[i + 1] = aggregateVariables.data[i];
                    }

                    aggregateVariables.data[row + 1] = new Object[]{
                        "" + aggregateVariables.data[row][0],
                        "" + aggregateVariables.data[row][1],
                        "" + aggregateVariables.data[row][2],
                        "" + aggregateVariables.data[row][3],
                        "" + aggregateVariables.data[row][4]};

                    int r = row + 1;

                    aggregateModel.fireTableDataChanged();
                    tableAggregate.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveUpBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAggregate.getSelectedRow();
                if (row > 0) {
                    Object[] rowData1 = aggregateVariables.data[row];
                    Object[] rowData2 = aggregateVariables.data[row - 1];

                    aggregateVariables.data[row] = rowData2;
                    aggregateVariables.data[row - 1] = rowData1;

                    int r = row - 1;

                    aggregateModel.fireTableDataChanged();

                    tableAggregate.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDownBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAggregate.getSelectedRow();
                if (row < aggregateVariables.data.length - 1) {
                    Object[] rowData1 = aggregateVariables.data[row];
                    Object[] rowData2 = aggregateVariables.data[row + 1];

                    aggregateVariables.data[row] = rowData2;
                    aggregateVariables.data[row + 1] = rowData1;

                    int r = row + 1;

                    aggregateModel.fireTableDataChanged();

                    tableAggregate.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        tableAggregate.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableAggregate.getSelectedRow();
                deleteBtn.setEnabled(row >= 0);
                editBtn.setEnabled(row >= 0);
                moveUpBtn.setEnabled(row > 0);
                duplicateBtn.setEnabled(row < aggregateVariables.data.length - 1);
                moveDownBtn.setEnabled(row < aggregateVariables.data.length - 1);
            }
        });
        aggregateButtons.add(deleteBtn);
        aggregateButtons.add(duplicateBtn);
        aggregateButtons.add(moveUpBtn);
        aggregateButtons.add(moveDownBtn);
        aggregateButtons.add(editBtn);

        comboBoxAggregate = new JComboBox();
        comboBoxFormatAggregate = new JComboBox();
        comboBoxFormatAggregate.setEditable(true);
        comboBoxFormatAggregate.addItem("");
        comboBoxFormatAggregate.addItem("0");
        comboBoxFormatAggregate.addItem("00");
        comboBoxFormatAggregate.addItem("000");
        comboBoxFormatAggregate.addItem("0.00");
        comboBoxAggregate.setEditable(true);
        comboBoxAggregate.addItem("average");
        comboBoxAggregate.addItem("sum");
        comboBoxAggregate.addItem("minumim");
        comboBoxAggregate.addItem("maximum");
        comboBoxAggregate.addItem("stdev");
        comboBoxAggregate.addItem("variance");
        comboBoxAggregate.addItem("kurtosis");
        comboBoxAggregate.addItem("skewness");
        comboBoxAggregate.addItem("percentile 10");
        comboBoxAggregate.addItem("first");
        comboBoxAggregate.addItem("count");

        tableAggregate.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBoxAggregate));

        tableAggregate.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(comboBoxFormatAggregate));

        editBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                editAggregateVar();
            }
        });

        JPanel wrapper = new JPanel();
        wrapper.add(aggregateButtons);
        return wrapper;
    }

    public void editFreeRowAggregateVar(String strVariable) {
        for (int i = 0; i < aggregateModel.getRowCount(); i++) {
            if (aggregateModel.getValueAt(i, 0).toString().isEmpty()) {
                aggregateModel.setValueAt(strVariable, i, 0);
                editAggregateVar(i);
                break;
            }
        }
    }

    public void editAggregateVar() {
        int row = tableAggregate.getSelectedRow();
        editAggregateVar(row);
    }

    public void editAggregateVar(int row) {
        if (row >= 0) {
            Object editors[] = new Object[AggregateVariables.aggregateVariables.columnNames.length];
            editors[0] = DataRowFrame.cloneComboBox(varCombo);
            editors[1] = DataRowFrame.cloneComboBox(comboBoxAggregate);
            editors[3] = DataRowFrame.cloneComboBox(varCombo);
            editors[4] = DataRowFrame.cloneComboBox(comboBoxFormatAggregate);
            new DataRowFrame(SketchletContext.getInstance().getEditorFrame(),
                    Language.translate("Aggregate"),
                    row,
                    AggregateVariables.aggregateVariables.columnNames,
                    editors, null, null,
                    tableAggregate,
                    aggregateModel);
        }
    }
}
