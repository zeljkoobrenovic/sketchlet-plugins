/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.SketchletContext;

/**
 *
 * @author zobrenovic
 */
public class ListUtils {

    public static String getLineText(String line) {
        int n1 = line.indexOf("{");
        if (n1 >= 0) {
            int n2 = line.indexOf("}", n1);
            if (n2 > n1) {
                line = line.substring(0, n1);
            }
        }
        return line;
    }

    public static String getLineCommand(String line) {
        int n1 = line.indexOf("{");
        if (n1 >= 0) {
            int n2 = line.indexOf("}", n1);
            if (n2 > n1) {
                return line.substring(n1 + 1, n2);
            }
        }

        return "";
    }

    public static void executeCommandIfDefined(String line) {
        String command = getLineCommand(line);
        if (!command.isEmpty()) {
            SketchletContext.getInstance().startCommandSequence(command);
        }
    }
}
