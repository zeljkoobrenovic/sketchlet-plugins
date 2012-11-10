/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.scripts.javascript;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.script.ScriptConsole;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author cuypers
 */
public class JS {

    static ScriptEngineManager mgr = new ScriptEngineManager(SketchletContext.getInstance().getPluginClassLoader());
    static ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    static Invocable invocableEngine = (Invocable) jsEngine;

    public static Object eval(String expression) {
        try {
            return jsEngine.eval(expression);
        } catch (Exception e) {
            e.printStackTrace();
            ScriptConsole.addLine("");
            ScriptConsole.addLine("* ERROR " + expression);
            ScriptConsole.addLine("    " + e.getMessage());
        }

        return "";
    }
}
