/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.derivedvars.standard;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.ui.VariableInterpolatorPanel;
import net.sf.sketchlet.plugin.AbstractPlugin;
import net.sf.sketchlet.plugin.DerivedVariablesPlugin;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.util.UtilContext;
import net.sf.sketchlet.util.XMLHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.Locale;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name="Numeric Mapping", type="derivedvariables")
public class VariableInterpolator extends AbstractPlugin implements VariableUpdateListener, DerivedVariablesPlugin {

    public static VariableInterpolator interpolator = new VariableInterpolator();
    Hashtable<String, String> lastUpdateVariable = new Hashtable<String, String>();

    public VariableInterpolator() {
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
        return new VariableInterpolatorPanel(this);
    }

    @Override
    public ImageIcon getIcon() {
        return UtilContext.getInstance().getImageIconFromResources("resources/number.png");
    }

    @Override
    public void onSave() {
        XMLHelper.save("derived_variables_numeric_mapping.xml", "numeric_mapping", data);
    }

    public void load() {
        XMLHelper.load("derived_variables_numeric_mapping.xml", "numeric_mapping", data);
        if (VariablesBlackboardContext.getInstance() != null) {
            VariablesBlackboardContext.getInstance().addVariableUpdateListener(this);
        }
    }
    public String columnNames[] = {Language.translate("Variable"), Language.translate("Min"), Language.translate("Max"), Language.translate("Format"), Language.translate("Derived Variable"), Language.translate("Min"), Language.translate("Max"), Language.translate("Format")};
    public Object data[][] = {
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""}};

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
        };

    }
    // boolean bUpdating = false;
    RowProtector rowsInProcessing = new RowProtector();

    @Override
    public synchronized void variableUpdated(String var, String value) {
        if (VariablesBlackboardContext.getInstance().isPaused()) {
            return;
        }
        //DataServer.protectVariable(var);
        for (int i = 0; i < data.length; i++) {
            if (rowsInProcessing.isRowProtected(i)) {
                continue;
            }
            String var1 = (String) data[i][0];
            String var2 = (String) data[i][4];
            if (!var1.isEmpty() && !var2.isEmpty() && !value.isEmpty() && (var.equalsIgnoreCase(var1) || var.equalsIgnoreCase(var2))) {
                rowsInProcessing.protectRow(i);
                String strMin1;
                String strMax1;
                String strMin2;
                String strMax2;
                String strFormat;
                if (var.equals(var1)) {
                    strMin1 = (String) data[i][1];
                    strMax1 = (String) data[i][2];
                    strMin2 = (String) data[i][5];
                    strMax2 = (String) data[i][6];
                    strFormat = (String) data[i][7];
                } else {
                    strMin1 = (String) data[i][5];
                    strMax1 = (String) data[i][6];
                    strMin2 = (String) data[i][1];
                    strMax2 = (String) data[i][2];
                    strFormat = (String) data[i][3];
                }

                double min1 = Double.NaN;
                double max1 = Double.NaN;
                double min2 = Double.NaN;
                double max2 = Double.NaN;

                try {
                    if (strMin1.length() > 0) {
                        min1 = Double.parseDouble(strMin1);
                    }
                    if (strMax1.length() > 0) {
                        max1 = Double.parseDouble(strMax1);
                    }
                    if (strMin2.length() > 0) {
                        min2 = Double.parseDouble(strMin2);
                    }
                    if (strMax2.length() > 0) {
                        max2 = Double.parseDouble(strMax2);
                    }

                    double doubleValue = Double.parseDouble(value);
                    double newValue = Double.NaN;

                    if (!Double.isNaN(min1)) {
                        if (!Double.isNaN(max1)) {
                            doubleValue = Math.max(Math.min(min1, max1), doubleValue);
                        } else {
                            doubleValue = Math.max(min1, doubleValue);
                        }
                    }
                    if (!Double.isNaN(max1)) {
                        if (!Double.isNaN(min1)) {
                            doubleValue = Math.min(Math.max(min1, max1), doubleValue);
                        } else {
                            doubleValue = Math.min(min1, doubleValue);
                        }
                    }

                    if (!Double.isNaN(min1) && !Double.isNaN(min2)) {
                        if (!Double.isNaN(max1) && !Double.isNaN(max2)) {
                            if (max1 >= min1) {
                                if (max2 > min2) {
                                    newValue = min2 + (max2 - min2) * ((doubleValue - min1) / (max1 - min1));
                                } else {
                                    newValue = min2 - (min2 - max2) * ((doubleValue - min1) / (max1 - min1));
                                }
                            } else {
                                if (max2 > min2) {
                                    newValue = min2 + (max2 - min2) * ((min1 - doubleValue) / (min1 - max1));
                                } else {
                                    newValue = min2 - (min2 - max2) * ((min1 - doubleValue) / (min1 - max1));
                                }
                            }
                        } else if (Double.isNaN(max2)) {
                            newValue = min2 + (doubleValue - min1);
                        } else {
                            newValue = doubleValue;
                        }
                    } else {
                        newValue = doubleValue;
                    }

                    if (!Double.isNaN(min2)) {
                        if (!Double.isNaN(max2)) {
                            doubleValue = Math.max(Math.min(min2, max2), doubleValue);
                        } else {
                            doubleValue = Math.max(min2, doubleValue);
                        }
                    }
                    if (!Double.isNaN(max2)) {
                        if (!Double.isNaN(min2)) {
                            doubleValue = Math.min(Math.max(min2, max2), doubleValue);
                        } else {
                            doubleValue = Math.min(min2, doubleValue);
                        }
                    }

                    DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                    String strNumber = strFormat.length() > 0 ? df.format(newValue) : "" + newValue;

                    if (var.equals(var1)) {
                        if (VariablesBlackboardContext.getInstance() != null) {
                            String strOldValue = lastUpdateVariable.get(var2);
                            if (strOldValue == null || !strNumber.equals(strOldValue)) {
                                VariablesBlackboardContext.getInstance().updateVariable(var2, strNumber);
                            }
                            lastUpdateVariable.put(var2, strNumber);
                        }
                    } else {
                        if (VariablesBlackboardContext.getInstance() != null) {
                            String strOldValue = lastUpdateVariable.get(var1);
                            if (strOldValue == null || !strNumber.equals(strOldValue)) {
                                VariablesBlackboardContext.getInstance().updateVariable(var1, strNumber);
                            }
                            lastUpdateVariable.put(var1, strNumber);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rowsInProcessing.unprotectRow(i);
            }
        }
        // DataServer.unprotectVariable(var);
    }

    public static void main(String args[]) {
        VariableInterpolator vi = new VariableInterpolator();
        vi.data[0][0] = "var1";
        vi.data[0][1] = "0";
        vi.data[0][2] = "100";
        vi.data[0][3] = "0";
        vi.data[0][4] = "var2";
        vi.data[0][5] = "0";
        vi.data[0][6] = "1";
        vi.data[0][7] = "0.00";

        vi.variableUpdated("var2", "0.23");
    }
}
