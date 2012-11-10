/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.derivedvars.standard;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.ui.AggregateVariablesPanel;
import net.sf.sketchlet.plugin.AbstractPlugin;
import net.sf.sketchlet.plugin.DerivedVariablesPlugin;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.util.RowProtector;
import net.sf.sketchlet.util.UtilContext;
import net.sf.sketchlet.util.XMLHelper;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

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
@PluginInfo(name="Aggregate", type="derivedvariables")
public class AggregateVariables extends AbstractPlugin implements VariableUpdateListener, DerivedVariablesPlugin {

    public static AggregateVariables aggregateVariables = new AggregateVariables();
    Hashtable<String, String> lastUpdateVariable = new Hashtable<String, String>();

    public AggregateVariables() {
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
        return new AggregateVariablesPanel(this);

    }

    @Override
    public ImageIcon getIcon() {
        return UtilContext.getInstance().getImageIconFromResources("resources/aggregate.png");
    }

    @Override
    public void onSave() {
        XMLHelper.save("derived_variables_aggregate.xml", "aggregate", data);
    }

    private void load() {
        if (data != null) {
            XMLHelper.load("derived_variables_aggregate.xml", "aggregate", data);
            if (VariablesBlackboardContext.getInstance() != null) {
                VariablesBlackboardContext.getInstance().removeVariableUpdateListener(this);
                VariablesBlackboardContext.getInstance().addVariableUpdateListener(this);
            }
        }
    }
    public String columnNames[] = {Language.translate("Variable"), Language.translate("Function"), Language.translate("Window Size"), Language.translate("Derived Variable"), Language.translate("Format")};
    public Object data[][] = {
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""}};

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
    // static boolean bUpdating = false;
    Hashtable<String, DescriptiveStatistics> allValues = new Hashtable<String, DescriptiveStatistics>();
    RowProtector rowsInProcessing = new RowProtector();

    @Override
    public synchronized void variableUpdated(String name, String value) {
        if (VariablesBlackboardContext.getInstance().isPaused() || name.length() == 0) {
            return;
        }
        //DataServer.protectVariable(triggerVariable);

        for (int v = 0; v < this.data.length; v++) {
            if (rowsInProcessing.isRowProtected(v)) {
                continue;
            }
            String variableName = (String) data[v][0];
            if (variableName.length() == 0) {
                continue;
            }
            if (name.equalsIgnoreCase(variableName)) {
                rowsInProcessing.protectRow(v);
                String strFunction = (String) data[v][1];
                String strWindow = (String) data[v][2];
                int window = strWindow.length() > 0 ? Integer.parseInt(strWindow) : 0;
                String strDerivedVariable = (String) data[v][3];
                if (strDerivedVariable.length() == 0) {
                    strDerivedVariable = variableName + "-" + strFunction.toLowerCase();
                    strDerivedVariable = strDerivedVariable.replace(" ", "-");
                }
                try {
                    DescriptiveStatistics statistics = allValues.get(strDerivedVariable + strWindow);
                    if (statistics == null) {
                        statistics = new DescriptiveStatistics(window <= 0 ? DescriptiveStatistics.INFINITE_WINDOW : window);
                        allValues.put(strDerivedVariable + strWindow, statistics);
                    }

                    statistics.addValue(Double.parseDouble(value));

                    String strFormat = (String) data[v][4];

                    if (strFunction.equalsIgnoreCase("minimum")) {
                        update(strDerivedVariable, statistics.getMin(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("maximum")) {
                        update(strDerivedVariable, statistics.getMax(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("average")) {
                        update(strDerivedVariable, statistics.getMean(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("sum")) {
                        update(strDerivedVariable, statistics.getSum(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("sumsq")) {
                        update(strDerivedVariable, statistics.getSumsq(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("stdev")) {
                        update(strDerivedVariable, statistics.getStandardDeviation(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("variance")) {
                        update(strDerivedVariable, statistics.getVariance(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("kurtosis")) {
                        update(strDerivedVariable, statistics.getKurtosis(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("skewness")) {
                        update(strDerivedVariable, statistics.getSkewness(), strFormat);
                    } else if (strFunction.equalsIgnoreCase("first")) {
                        update(strDerivedVariable, statistics.getElement(0), strFormat);
                    } else if (strFunction.equalsIgnoreCase("count")) {
                        update(strDerivedVariable, statistics.getN(), strFormat);
                    } else if (strFunction.toLowerCase().startsWith("percentile")) {
                        String params[] = strFunction.split(" ");
                        if (params.length > 1) {
                            double p = Double.parseDouble(params[1]);
                            update(strDerivedVariable, statistics.getPercentile(p), strFormat);
                        }
                    }

                } catch (Exception e) {
                    // e.printStackTrace();
                }

                rowsInProcessing.unprotectRow(v);
            }
        }
        // DataServer.unprotectVariable(triggerVariable);
        // bUpdating = false;
    }

    public void update(String var, double value, String strFormat) {
        DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
        String strNumber = strFormat.length() > 0 ? df.format(value) : "" + value;
        String strOldValue = lastUpdateVariable.get(var);
        if (strOldValue == null || !strNumber.equals(strOldValue)) {
            VariablesBlackboardContext.getInstance().updateVariable(var, strNumber);
        }
        lastUpdateVariable.put(var, strNumber);
    }
}
