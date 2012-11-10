/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author zobrenovic
 */
public class TestUMLGraph {

    public static void main(String args[]) {
        PrintWriter err = new PrintWriter(new StringWriter());
        com.sun.tools.javadoc.Main.execute("UmlGraph",
                err, err, err, "org.umlgraph.doclet.UmlGraph", new String[]{"-package", "-output", "d:\\temp\\Person.dot", "d:\\temp\\Person.java"});
    }
}
