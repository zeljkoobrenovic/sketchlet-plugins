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
import net.sf.sketchlet.plugins.widgets.graphs.utils.DestroyThread;
import net.sf.sketchlet.uml.ExternalPrograms;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Graphviz dot", type = "widget", group = "Graphs", position = 252)
@WidgetPluginTextItems(initValue = "digraph G {\n    main -> parse -> execute;\n    main -> init;\n    main -> cleanup;\n    execute -> make_string;\n    execute -> printf;\n    init -> make_string;\n    main -> printf;\n    execute -> compare;\n    }")
@WidgetPluginLinks(link = "http://www.graphviz.org/Documentation.php")
public class WidgetGraphvizDot extends ExternalImageProgramGeneratorWidget {

    @WidgetPluginProperty(name = "dot parameters", initValue = "", description = "Additional parameters to be sent to the dot program", valueList = {"-Gratio=0.7 -Eminlen=2"})
    private String cmdLineParams = "";

    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size", valueList = {"true", "false"})
    private boolean resizeRegionEnabled = true;

    public WidgetGraphvizDot(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void callImageGenerator() {
        String strDot = getActiveRegionContext().getWidgetItemText();
        strDot = prepareDot(strDot);
        File dotFile = null;
        File imgFile = null;
        try {
            dotFile = File.createTempFile("umlgraph", ".dot");
            imgFile = File.createTempFile("umlgraph", ".png");
            FileUtils.saveFileText(dotFile, strDot);

            List<String> dotParams = new ArrayList<String>();

            dotParams.add(ExternalPrograms.getGraphVizDotPath());
            dotParams.add("-Tpng");
            String strDotParams = getCmdLineParams();
            String params[] = strDotParams.split(" ");
            for (String param : params) {
                dotParams.add(param);
            }

            dotParams.add("-o" + imgFile.getAbsolutePath());
            dotParams.add(dotFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(dotParams.toArray(new String[dotParams.size()]));
            processBuilder.directory(new File(ExternalPrograms.getGraphVizDotPath()).getParentFile());
            Process theProcess = processBuilder.start();
            DestroyThread dt = new DestroyThread(theProcess);
            theProcess.waitFor();

            if (imgFile.exists()) {
                BufferedImage image = ImageIO.read(imgFile);
                if (image != null) {
                    this.setImage(image);
                    if (isResizeRegionEnabled() && this.getActiveRegionContext() != null) {
                        this.getActiveRegionContext().setProperty("width", "" + image.getWidth());
                        this.getActiveRegionContext().setProperty("height", "" + image.getHeight());
                        setScaling(false);
                    } else {
                        setScaling(true);
                    }
                }
                setTimeout(false);
            } else {
                SketchletPluginLogger.error("Could not generate Graphwiz DOT image.");
                setTimeout(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setTimeout(true);
        } finally {
            if (dotFile != null) {
                dotFile.delete();
            }
            if (imgFile != null) {
                imgFile.delete();
            }
            setResizeRegionEnabled(false);
        }
    }

    protected String prepareDot(String text) {
        return text;
    }

    public String getCmdLineParams() {
        return cmdLineParams;
    }

    public void setCmdLineParams(String cmdLineParams) {
        this.cmdLineParams = cmdLineParams;
    }

    public boolean isResizeRegionEnabled() {
        return resizeRegionEnabled;
    }

    public void setResizeRegionEnabled(boolean resizeRegionEnabled) {
        this.resizeRegionEnabled = resizeRegionEnabled;
    }
}
