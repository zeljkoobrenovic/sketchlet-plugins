/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "UMLGraph / Sequence Diagram", type = "widget", group = "UML", position = 265)
@WidgetPluginTextItems(initValue = ".PS" + "\n"
        + "copy \"sequence.pic\";" + "\n"
        + "" + "\n"
        + "# Define the objects" + "\n"
        + "object(O,\"o:Toolkit\");" + "\n"
        + "placeholder_object(P);" + "\n"
        + "step();" + "\n"
        + "" + "\n"
        + "# Message sequences" + "\n"
        + "active(O);" + "\n"
        + "step();" + "\n"
        + "active(O);" + "\n"
        + "message(O,O,\"callbackLoop()\");" + "\n"
        + "inactive(O);" + "\n"
        + "create_message(O,P,\"p:Peer\");" + "\n"
        + "message(O,P,\"handleExpose()\");" + "\n"
        + "active(P);" + "\n"
        + "return_message(P,O,\"\");" + "\n"
        + "inactive(P);" + "\n"
        + "destroy_message(O,P);" + "\n"
        + "inactive(O);" + "\n"
        + "" + "\n"
        + "# Complete the lifelines" + "\n"
        + "step();" + "\n"
        + "complete(O);" + "\n"
        + ".PE" + "\n")
@WidgetPluginLinks(links = {
        "UML Graph Documentation Page; http://www.umlgraph.org/doc/seq-intro.html"
})
public class WidgetUMLGraphSequence extends WidgetPic2Plot {

    @WidgetPluginProperty(name = "scale", initValue = "1.34", description = "Internal image scaling")
    private double scale = 1.34;

    @WidgetPluginProperty(name = "pic2plot parameters", initValue = "", description = "Additional parameters to be sent to the plotutils program")
    private String cmdLineParams = "";

    public WidgetUMLGraphSequence(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public String getPicText() {
        super.setCmdLineParams(this.cmdLineParams);
        super.setScale(this.scale);
        return getActiveRegionContext().getWidgetItemText();
    }
}
