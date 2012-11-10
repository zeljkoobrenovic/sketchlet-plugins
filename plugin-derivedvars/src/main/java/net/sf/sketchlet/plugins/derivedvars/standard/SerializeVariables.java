/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.derivedvars.standard;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.ui.SerializeVariablesPanel;
import net.sf.sketchlet.plugin.AbstractPlugin;
import net.sf.sketchlet.plugin.DerivedVariablesPlugin;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.util.UtilContext;
import net.sf.sketchlet.util.XMLHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Hashtable;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name="Serialize", type="derivedvariables")
public class SerializeVariables extends AbstractPlugin implements VariableUpdateListener, DerivedVariablesPlugin {

    public static SerializeVariables serializeVariables = new SerializeVariables();

    public SerializeVariables() {
        //load();
    }

    @Override
    public void afterProjectOpening() {
        load();
    }

    @Override
    public void beforeProjectClosing() {
        onSave();
    }

    @Override
    public void afterApplicationStart() {
    }

    @Override
    public void beforeApplicationEnd() {
    }

    @Override
    public Component getGUI() {
        return new SerializeVariablesPanel(this);

    }

    @Override
    public ImageIcon getIcon() {
        return UtilContext.getInstance().getImageIconFromResources("resources/serialize.png");
    }

    @Override
    public void onSave() {
        XMLHelper.save("derived_variables_serialize.xml", "serialize", data);
    }

    public void load() {
        XMLHelper.load("derived_variables_serialize.xml", "serialize", data);
        if (VariablesBlackboardContext.getInstance() != null) {
            VariablesBlackboardContext.getInstance().addVariableUpdateListener(this);
        }
    }
    public String columnNames[] = {Language.translate("Variable"), Language.translate("Prefix"), Language.translate("Count Variable"), Language.translate("Start"), Language.translate("End"), Language.translate("Loop")};
    public Object data[][] = {
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)},
        {"", "", "", "", "", new Boolean(false)}};

    public AbstractTableModel getTableModel() {
        return new AbstractTableModel() {

            public int getColumnCount() {
                return columnNames.length;
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public int getRowCount() {
                return data.length;
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public void setValueAt(Object value, int row, int col) {
                data[row][col] = value;
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public Class getColumnClass(int col) {
                return data[0][col].getClass();
            }
        };
    }
    // static boolean bUpdating = false;
    RowProtector rowsInProcessing = new RowProtector();
    Hashtable<String, Integer> countValues = new Hashtable<String, Integer>();

    @Override
    public synchronized void variableUpdated(String name, String value) {
        if (VariablesBlackboardContext.getInstance().isPaused() || name.length() == 0) {
            return;
        }
        //DataServer.protectVariable(triggerVariable);
        // bUpdating = true;

        for (int i = 0; i < data.length; i++) {
            if (rowsInProcessing.isRowProtected(i)) {
                continue;
            }
            String variableName = (String) data[i][0];

            if (variableName.equalsIgnoreCase(name)) {
                rowsInProcessing.protectRow(i);
                String prefix = (String) data[i][1];
                if (prefix.length() == 0) {
                    prefix = variableName + "-";
                }
                String countVar = (String) data[i][2];
                String strStart = (String) data[i][3];
                String strEnd = (String) data[i][4];
                boolean loop = ((Boolean) data[i][5]).booleanValue();
                int endIndex = strEnd.length() > 0 ? Integer.parseInt(strEnd) : 0;
                Integer countValue = countValues.get(prefix);
                int count = strStart.length() > 0 ? Integer.parseInt(strStart) : 1;
                if (countValue != null) {
                    count = countValue.intValue();
                }

                if (endIndex > 0 && count > endIndex) {
                    if (loop) {
                        try {
                            count = strStart.length() > 0 ? Integer.parseInt(strStart) : 1;
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
                    } else {
                        return;
                    }
                }

                VariablesBlackboardContext.getInstance().updateVariable(prefix + count, value);

                if (countVar.equals("")) {
                    VariablesBlackboardContext.getInstance().updateVariable(countVar, "" + count);
                }

                countValues.put(prefix, new Integer(count + 1));

                rowsInProcessing.unprotectRow(i);
            }
        }
        // DataServer.unprotectVariable(triggerVariable);

        // bUpdating = false;
    }
}
