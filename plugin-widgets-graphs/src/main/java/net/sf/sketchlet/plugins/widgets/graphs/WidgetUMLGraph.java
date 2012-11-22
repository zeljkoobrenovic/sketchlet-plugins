/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.net.logger.SketchletPluginLogger;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;
import net.sf.sketchlet.uml.ExternalPrograms;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "UMLGraph / Class Diagram", type = "widget", group = "UML", position = 260)
@WidgetPluginTextItems(initValue = "class PersonTest {\n    String NameTest;\n}\nclass EmployeeTest extends PersonTest {}\nclass ClientTest extends PersonTest {}")
@WidgetPluginLinks(links = {
        "UML Graph Documentation Page; http://www.umlgraph.org/doc.html"
})
public class WidgetUMLGraph extends ExternalImageProgramGeneratorWidget {

    @WidgetPluginProperty(name = "dot parameters", initValue = "", description = "Additional parameters to be sent to the dot program", valueList = {"-Gratio=0.7 -Eminlen=2"})
    private String cmdLineParams = "";

    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size")
    private boolean resizingRegion = true;

    public WidgetUMLGraph(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void callImageGenerator() {
        final String strUML = getUMLGraphCode();
        File srcFile = null;
        File dotFile = null;
        File imgFile = null;
        try {
            srcFile = File.createTempFile("umlgraph", ".java");
            dotFile = File.createTempFile("umlgraph", ".dot");
            imgFile = File.createTempFile("umlgraph", ".png");
            FileUtils.saveFileText(srcFile, strUML);
            // PrintWriter err = new PrintWriter(new StringWriter());
            StringWriter strw = new StringWriter();
            PrintWriter err = new PrintWriter(strw);
            com.sun.tools.javadoc.Main.execute("UmlGraph",
                    err, err, err, "net.sf.sketchlet.umlgraph.doclet.UmlGraph", new String[]{"-package", "-output", dotFile.getAbsolutePath(), srcFile.getAbsolutePath()});

            SketchletPluginLogger.debug(strw.toString());

            if (this.getActiveRegionContext() == null) {
                return;
            }

            List<String> dotParams = new ArrayList<String>();

            dotParams.add(ExternalPrograms.getGraphVizDotPath());
            dotParams.add("-Tpng");

            String params[] = getCmdLineParams().split(" ");
            for (String param : params) {
                dotParams.add(param);
            }

            dotParams.add("-o" + imgFile.getAbsolutePath());
            dotParams.add(dotFile.getAbsolutePath());

            ProcessBuilder processBuilder2 = new ProcessBuilder(dotParams.toArray(new String[dotParams.size()]));
            processBuilder2.directory(new File(ExternalPrograms.getGraphVizDotPath()).getParentFile());
            Process theProcess2 = processBuilder2.start();
            new DestroyThread(theProcess2);
            theProcess2.waitFor();

            if (imgFile.exists()) {
                BufferedImage image = ImageIO.read(imgFile);
                this.setImage(image);
                if (isResizingRegion() && image != null) {
                    this.getActiveRegionContext().setProperty("width", "" + image.getWidth());
                    this.getActiveRegionContext().setProperty("height", "" + image.getHeight());
                    setScaling(false);
                } else {
                    setScaling(true);
                }
                setTimeout(false);
            } else {
                SketchletPluginLogger.error("Could not generate UML Graph image.");
                setTimeout(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setTimeout(true);
        } finally {
            if (srcFile != null) {
                srcFile.delete();
            }
            if (dotFile != null) {
                dotFile.delete();
            }
            if (imgFile != null) {
                imgFile.delete();
            }
        }
    }

    protected String getUMLGraphCode() {
        return getActiveRegionContext().getWidgetItemText();
    }

    public String getCmdLineParams() {
        return cmdLineParams;
    }

    public void setCmdLineParams(String cmdLineParams) {
        this.cmdLineParams = cmdLineParams;
    }

    public boolean isResizingRegion() {
        return resizingRegion;
    }

    public void setResizingRegion(boolean resizingRegion) {
        this.resizingRegion = resizingRegion;
    }
}
