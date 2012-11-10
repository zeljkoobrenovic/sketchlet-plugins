/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.derivedvars.standard;

import java.util.Enumeration;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;

/**
 *
 * @author zobrenovic
 */
public class JEParser {

    public static CellReferenceResolver resolver = new CellReferenceResolver();

    public static void setResolver(CellReferenceResolver _resolver) {
        JEParser.resolver = _resolver;
    }

    public static Object getValue(String strExpression) {
        // DJep parser = new DJep();
        JEP parser = new JEP();
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.addStandardConstants();
        //parser.addStandardDiffRules();

        parser.initSymTab(); // clear the contents of the symbol table
        parser.setAllowUndeclared(true);
        try {
            parser.parseExpression(strExpression);

            SymbolTable st = parser.getSymbolTable();
            Enumeration e = st.elements();
            while (e.hasMoreElements()) {
                Variable v = (Variable) e.nextElement();
                try {
                    v.setValue(new Double(resolver.getValue(v.getName())));
                } catch (Exception ex) {
                    String strValue = resolver.getValue(v.getName());
                    if (strValue == null) {
                        return null;
                    }
                    v.setValue(strValue);
                }
            }
        } catch (Exception e) {
        }

        return parser.getValueAsObject();
    }

    /*    public static Object getValue(String strExpression) {
    JEP parser = new JEP();
    parser.initFunTab(); // clear the contents of the function table
    parser.addStandardFunctions();
    parser.addStandardConstants();
    
    parser.initSymTab(); // clear the contents of the symbol table
    parser.setAllowUndeclared(true);
    parser.parseExpression(strExpression);
    
    SymbolTable st = parser.getSymbolTable();
    Enumeration e = st.elements();
    while (e.hasMoreElements()) {
    Variable v = (Variable) e.nextElement();
    try {
    v.setValue(new Double(DataServer.variablesServer.getVariableValue(v.getName())));
    } catch (Exception ex) {
    String strValue = DataServer.variablesServer.getVariableValue(v.getName());
    if (strValue == null) {
    return null;
    }
    v.setValue(strValue);
    }
    }
    
    return parser.getValueAsObject();
    }*/
    public static void main(String args[]) {
        System.out.println(getValue("a + 3"));
    }

    public static void help(JFrame frame) {
        String strHelp = "";
        JEP p = new JEP();
        p.addStandardFunctions();
        Enumeration en = p.getFunctionTable().elements();

        String operators[] = {
            "^",
            "!",
            "+x, -x",
            "%",
            "/",
            "*",
            "+, -",
            "<=, >=",
            "<, >",
            "!=, ==",
            "&&",
            "||"};

        String functions[] = {
            "sin(x)",
            "cos(x)",
            "tan(x)",
            "asin(x)",
            "acos(x)",
            "atan(x)",
            "atan2(y, x)",
            "sinh(x)",
            "cosh(x)",
            "tanh(x)",
            "asinh(x)",
            "acosh(x)",
            "atanh(x)",
            "ln(x)",
            "log(x)",
            "exp(x)",
            "abs(x)",
            "rand()",
            "mod(x,y) = x % y",
            "sqrt(x)",
            "sum(x,y,z)",
            "if(cond,trueval,falseval)",
            "str(x)",
            "binom(n,i)"
        };

        strHelp += "Operators: \n";
        for (int i = 0; i < operators.length; i++) {
            strHelp += "     " + operators[i] + "\n";
        }

        strHelp += "Functions: \n";
        for (int i = 0; i < functions.length; i++) {
            strHelp += "     " + functions[i] + "\n";
        }
        strHelp += "NOTE: Variables with names that contain operators such as \"-\"\nshould be giving in apostrophes, for example, 'movement-intensity'.";

        JOptionPane.showMessageDialog(frame, strHelp);
    }
}
