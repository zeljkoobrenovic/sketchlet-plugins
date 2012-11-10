/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "PlotUtils Pic2Plot", type = "widget", group="Graphs", position = 255)
@WidgetPluginTextItems(initValue = ".PS\n"
+ "box \"box\";\n"
+ "move;\n"
+ "line \"line\" \"\";\n"
+ "move;\n"
+ "arrow \"arrow\" \"\";\n"
+ "move;\n"
+ "circle \"circle\";\n"
+ "move;\n"
+ "ellipse \"ellipse\";\n"
+ "move;\n"
+ "arc; down; move; \"arc\"\n"
+ ".PE")
@WidgetPluginLinks(links = {
    "Pic2Plot Examples; http://trac.wikidpad2.webfactional.com/wiki/Pic2Plot"
})
public class WidgetPic2PlotNormal extends WidgetPic2Plot {

    public WidgetPic2PlotNormal(ActiveRegionContext region) {
        super(region);
    }
    @WidgetPluginProperty(name = "scale", initValue = "1.34",
    description = "Internal image scaling")
    protected double scale = 1.34;
    @WidgetPluginProperty(name = "pic2plot parameters", initValue = "",
    description = "Additional parameters to be sent to the plotutils program")
    protected String cmdLineParams = "";

    @Override
    protected String getPicText() {
        super.cmdLineParams = this.cmdLineParams;
        super.scale = this.scale;
        return getActiveRegionContext().getWidgetItemText();
    }
}
