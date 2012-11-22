/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;
import net.sf.sketchlet.plugins.widgets.graphs.utils.DestroyThread;
import net.sf.sketchlet.uml.ExternalPrograms;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "UMLGraph / Sequence Diagram / Simple DSL", type = "widget", group = "UML", position = 270)
@WidgetPluginTextItems(initValue = "S,\"s:Switch\"" + "\n"
        + "P,\"p:Pump\"" + "\n"
        + "|\n"
        + "S [\n"
        + "  P [\n"
        + "    S => P, \"run()\"\n"
        + "    S => P, \"stop()\"\n"
        + "    |\n"
        + "  ] P\n"
        + "] S")
@WidgetPluginLinks(links = {
        "DSL for UMLGraph Sequence Diagram; http://macroexpand.org/doku.php?id=articles:uml-sequence-diagram-dsl-txl:start"
})
public class WidgetUMLGraphSequenceSimple extends WidgetPic2Plot {

    @WidgetPluginProperty(name = "scale", initValue = "1.34", description = "Internal image scaling")
    private double thisScale = 1.34;

    @WidgetPluginProperty(name = "pic2plot parameters", initValue = "", description = "Additional parameters to be sent to the plotutils program")
    private String thisCmdLineParams = "";

    private Random generator = new Random();
    private String strResult = "";

    public WidgetUMLGraphSequenceSimple(ActiveRegionContext region) {
        super(region);
    }

    @Override
    protected String getPicText() {
        super.setCmdLineParams(this.thisCmdLineParams);
        super.setScale(this.thisScale);

        File tempDir = new File(SketchletContextUtils.getCurrentProjectDir() + "temp/umlgraphseqdsl" + generator.nextInt());
        try {
            String strText = getActiveRegionContext().getWidgetItemText();
            tempDir.mkdirs();
            FileUtils.restore(SketchletContextUtils.getSketchletDesignerConfDir() + "/umlgraphseq", tempDir.getPath());

            File seqFile = new File(tempDir, "seq.sqd");
            FileUtils.saveFileText(seqFile, strText);

            List<String> txlParams = new ArrayList<String>();

            txlParams.add(ExternalPrograms.getTxlPath());
            txlParams.add("sample0.pic");
            txlParams.add("-");
            txlParams.add("-sqdfile");
            txlParams.add(seqFile.getName());

            ProcessBuilder processBuilder = new ProcessBuilder(txlParams.toArray(new String[txlParams.size()]));
            processBuilder.directory(tempDir);

            Process theProcess = processBuilder.start();
            DestroyThread dt = new DestroyThread(theProcess);
            BufferedReader bri = new BufferedReader(new InputStreamReader(theProcess.getInputStream()));
            strResult = "";
            String line;
            while ((line = bri.readLine()) != null) {
                strResult += line + "\n";
            }
            BufferedReader bre = new BufferedReader(new InputStreamReader(theProcess.getErrorStream()));
            while (bre.readLine() != null) {
            }
            theProcess.waitFor();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            //FileUtils.deleteDir(tempDir);
        }
        return strResult;
    }
}
