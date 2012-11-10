/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.derivedvars.standard;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugins.derivedvars.standard.ui.FormulaVariablesPanel;
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
@PluginInfo(name="Formulas", type="derivedvariables")
public class FormulaVariables extends AbstractPlugin implements VariableUpdateListener, DerivedVariablesPlugin {

    public static FormulaVariables formulas = new FormulaVariables();
    Hashtable<String, String> lastUpdateVariable = new Hashtable<String, String>();

    public FormulaVariables() {
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
    public Component getGUI() {
        return new FormulaVariablesPanel(this);
    }

    @Override
    public ImageIcon getIcon() {
        return UtilContext.getInstance().getImageIconFromResources("resources/formula.png");
    }

    @Override
    public void onSave() {
        XMLHelper.save("derived_variables_formulas.xml", "formulas", data);
    }

    public void load() {
        XMLHelper.load("derived_variables_formulas.xml", "formulas", data);
        if (VariablesBlackboardContext.getInstance() != null) {
            VariablesBlackboardContext.getInstance().addVariableUpdateListener(this);
        }
    }
    public String columnNames[] = {Language.translate("Update Variable"), Language.translate("Formula"), Language.translate("Format"), Language.translate("Status")};
    public Object data[][] = {
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""}};

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
    RowProtector rowsInProcessing = new RowProtector();

    @Override
    public synchronized void variableUpdated(String var, String value) {
        //DataServer.protectVariable(var);
        refreshFormula();
        // DataServer.unprotectVariable(var);
    }

    public synchronized void refreshFormula() {
        if (VariablesBlackboardContext.getInstance().isPaused()) {
            return;
        }
        for (int i = 0; i < data.length; i++) {
            if (rowsInProcessing.isRowProtected(i)) {
                continue;
            }
            String newVar = (String) data[i][0];
            String strFormula = (String) data[i][1];
            strFormula = getTemplateFromApostrophes(strFormula);
            strFormula = VariablesBlackboardContext.getInstance().populateTemplate(strFormula);

            if (!newVar.isEmpty() && !strFormula.isEmpty()) {
                rowsInProcessing.protectRow(i);
                // MathEvaluator math = new MathEvaluator(strFormula);
                Object result = JEParser.getValue(strFormula);

                if (result == null) {
                    data[i][3] = "Error in formula";
                    this.getTableModel().fireTableDataChanged();
                } else {
                    String strFormat = (String) data[i][2];
                    if (!strFormat.startsWith("0") && !strFormat.startsWith("#")) {
                        strFormat = "";
                        //data[i][2] = "";
                    }
                    DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                    String strNumber = "";
                    if (result != null) {
                        if (result instanceof Double && !strFormat.isEmpty()) {
                            strNumber = df.format(result);
                        } else {
                            strNumber = result.toString();
                        }
                    }

                    String oldValue = VariablesBlackboardContext.getInstance().getVariableValue(newVar);
                    if (!oldValue.equals(strNumber)) {
                        VariablesBlackboardContext.getInstance().updateVariable(newVar, strNumber);
                        data[i][3] = result.toString();
                        this.getTableModel().fireTableDataChanged();
                    }
                }

                rowsInProcessing.unprotectRow(i);
            }
        }
    }

    public static boolean isInStringFunction(String exp, int n) {

        if (n > exp.length()) {
            exp = exp.substring(0, n).trim().toLowerCase();
            if (exp.endsWith("substring(")) {
                return true;
            }
            if (exp.endsWith("mid(")) {
                return true;
            }
            if (exp.endsWith("left(")) {
                return true;
            }
            if (exp.endsWith("right(")) {
                return true;
            }
        }

        return false;
    }

    public static String getTemplateFromApostrophes(String expression) {
        int _n = 0;
        String extra = "";
        while (expression.contains("'")) {
            if (_n % 2 == 0) {
                extra = "";
                if (isInStringFunction(expression, _n)) {
                    extra = "\"";
                }
                int n1 = expression.indexOf("'");
                if (n1 >= 0) {
                    int n2 = expression.indexOf("'", n1 + 1);
                    if (n2 > n1) {
                        String str = VariablesBlackboardContext.getInstance().getVariableValue(expression.substring(n1 + 1, n2));
                        boolean bNum = true;
                        try {
                            double d = Double.parseDouble(str);
                        } catch (Exception e) {
                            bNum = false;
                        }
                        if (!bNum) {
                            extra = "\"";
                        }
                    }
                }
                expression = expression.replaceFirst("'", extra + "<%=");
            } else {
                expression = expression.replaceFirst("'", "%>" + extra);
                extra = "";
            }
            _n++;
        }

        return expression;
    }
}
