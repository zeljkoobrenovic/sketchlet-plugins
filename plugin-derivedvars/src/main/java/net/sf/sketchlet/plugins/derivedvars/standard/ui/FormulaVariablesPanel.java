/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.plugins.derivedvars.standard.ui;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.FormulaVariables;
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
import javax.swing.table.TableColumn;

/**
 *
 * @author zobrenovic
 */
public class FormulaVariablesPanel extends JPanel {

    JTable tableFormulas;
    AbstractTableModel formulasModel;
    FormulaVariables formulaVariables;
    JComboBox varCombo = new JComboBox();
    JComboBox comboBoxFormatFormulas = new JComboBox();

    public FormulaVariablesPanel(FormulaVariables formulaVariables) {
        this.formulaVariables = formulaVariables;
        setLayout(new BorderLayout());
        VariablesBlackboardContext.getInstance().populateVariablesCombo(varCombo, false);

        formulasModel = formulaVariables.getTableModel();
        tableFormulas = new JTable(formulasModel);
        TableColumn col = tableFormulas.getColumnModel().getColumn(0);
        col.setPreferredWidth(120);
        col.setMaxWidth(120);
        col = tableFormulas.getColumnModel().getColumn(2);
        col.setPreferredWidth(120);
        col.setMaxWidth(120);
        col = tableFormulas.getColumnModel().getColumn(3);
        col.setPreferredWidth(120);
        col.setMaxWidth(120);
        tableFormulas.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableFormulas.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(varCombo));

        JScrollPane scrollPaneFormulas = new JScrollPane(tableFormulas);
        JPanel formulaFieldPanel = new JPanel(new BorderLayout());
        this.add(formulaFieldPanel, BorderLayout.NORTH);
        this.add(scrollPaneFormulas, BorderLayout.CENTER);
        this.add(getFormulasButtons(), BorderLayout.EAST);

        setDragAndDropHandlers();
    }

    public void setDragAndDropHandlers() {

        new FileDrop(System.out, tableFormulas, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (strText.startsWith("=") && strText.length() > 1) {
                    for (int i = 0; i < formulaVariables.data.length; i++) {
                        if (formulaVariables.data[i][0].toString().isEmpty()) {
                            formulaVariables.data[i][0] = strText.substring(1);
                            editFormulaRow(i);
                            break;
                        }
                    }
                }
                DataRowFrame.emptyOnCancel = false;
            }
        });
    }

    public void save() {
        this.formulaVariables.onSave();
    }

    public void load() {
        this.formulaVariables.load();
    }
    static JComboBox lastComboBoxParam1 = null;

    public JPanel getFormulasButtons() {
        JPanel formulasButtons = new JPanel(new GridLayout(0, 1));
        final JButton deleteBtn = new JButton(Language.translate("Delete"));
        final JButton duplicateBtn = new JButton(Language.translate("Duplicate"));
        final JButton moveUpBtn = new JButton(Language.translate("Move Up"));
        final JButton moveDownBtn = new JButton(Language.translate("Move Down"));
        final JButton editBtn = new JButton(Language.translate("Edit"));

        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableFormulas.getSelectedRow();
                if (row >= 0) {
                    int r = row;

                    for (int i = row; i < formulaVariables.data.length - 1; i++) {
                        formulaVariables.data[i] = formulaVariables.data[i + 1];
                    }

                    formulaVariables.data[formulaVariables.data.length - 1] = new Object[]{"", "", "", ""};

                    formulasModel.fireTableDataChanged();

                    tableFormulas.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        duplicateBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableFormulas.getSelectedRow();
                if (row < formulaVariables.data.length - 1) {
                    for (int i = formulaVariables.data.length - 2; i >= row + 1; i--) {
                        formulaVariables.data[i + 1] = formulaVariables.data[i];
                    }

                    formulaVariables.data[row + 1] = new Object[]{
                        "" + formulaVariables.data[row][0],
                        "" + formulaVariables.data[row][1],
                        "" + formulaVariables.data[row][2],
                        "" + formulaVariables.data[row][3]};

                    int r = row + 1;

                    formulasModel.fireTableDataChanged();
                    tableFormulas.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveUpBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableFormulas.getSelectedRow();
                if (row > 0) {
                    Object[] rowData1 = formulaVariables.data[row];
                    Object[] rowData2 = formulaVariables.data[row - 1];

                    formulaVariables.data[row] = rowData2;
                    formulaVariables.data[row - 1] = rowData1;

                    int r = row - 1;

                    formulasModel.fireTableDataChanged();

                    tableFormulas.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDownBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableFormulas.getSelectedRow();
                if (row < formulaVariables.data.length - 1) {
                    Object[] rowData1 = formulaVariables.data[row];
                    Object[] rowData2 = formulaVariables.data[row + 1];

                    formulaVariables.data[row] = rowData2;
                    formulaVariables.data[row + 1] = rowData1;

                    int r = row + 1;

                    formulasModel.fireTableDataChanged();

                    tableFormulas.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        tableFormulas.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableFormulas.getSelectedRow();
                deleteBtn.setEnabled(row >= 0);
                editBtn.setEnabled(row >= 0);
                moveUpBtn.setEnabled(row > 0);
                duplicateBtn.setEnabled(row < formulaVariables.data.length - 1);
                moveDownBtn.setEnabled(row < formulaVariables.data.length - 1);
            }
        });
        formulasButtons.add(deleteBtn);
        formulasButtons.add(duplicateBtn);
        formulasButtons.add(moveUpBtn);
        formulasButtons.add(moveDownBtn);
        formulasButtons.add(editBtn);

        editBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableFormulas.getSelectedRow();
                editFormulaRow(row);
            }
        });

        comboBoxFormatFormulas = new JComboBox();
        comboBoxFormatFormulas.setEditable(true);
        comboBoxFormatFormulas.addItem("");
        comboBoxFormatFormulas.addItem("0");
        comboBoxFormatFormulas.addItem("00");
        comboBoxFormatFormulas.addItem("000");
        comboBoxFormatFormulas.addItem("0.00");
        tableFormulas.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBoxFormatFormulas));

        JPanel wrapper = new JPanel();
        wrapper.add(formulasButtons);
        return wrapper;
    }

    public void editFormulaFreeRow(String strVariable) {
        for (int i = 0; i < formulasModel.getRowCount(); i++) {
            if (formulasModel.getValueAt(i, 0).toString().isEmpty()) {
                formulasModel.setValueAt(strVariable, i, 0);
                editFormulaRow(i);
                break;
            }
        }
    }

    public void editFormulaRow(int row) {
        if (row >= 0) {
            Object editors[] = new Object[FormulaVariables.formulas.columnNames.length];
            editors[0] = DataRowFrame.cloneComboBox(varCombo);
            editors[3] = DataRowFrame.cloneComboBox(comboBoxFormatFormulas);
            new DataRowFrame(SketchletContext.getInstance().getEditorFrame(),
                    Language.translate("Formula"),
                    row,
                    FormulaVariables.formulas.columnNames,
                    editors, null, null,
                    tableFormulas,
                    formulasModel);
        }
    }
}
