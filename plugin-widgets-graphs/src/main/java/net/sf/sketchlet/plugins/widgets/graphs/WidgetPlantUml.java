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
import net.sf.sketchlet.uml.ExternalPrograms;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "PlantUML", type = "widget", group="UML", position = 259)
@WidgetPluginTextItems(initValue = "@startuml\nBob -> Alice : hello\n@enduml\n")
@WidgetPluginLinks(links = {
    "PlantUML Home Page; http://plantuml.sourceforge.net/"
})
public class WidgetPlantUml extends ExternalImageProgramCallerWidget {

    //
    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size")
    protected boolean resizeRegion = true;

    public WidgetPlantUml(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void callImageGenerator() {
        prevTime = System.currentTimeMillis();
        try {
            if (this.getActiveRegionContext() == null) {
                return;
            }
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            String source = this.getActiveRegionContext().getWidgetItemText().trim();

            if (!source.startsWith("@startuml")) {
                source = "@startuml\n" + source;
            }

            if (!source.endsWith("@startuml")) {
                source += "\n@enduml";
            }
            SourceStringReader reader = new SourceStringReader(source);
            if (System.getenv("GRAPHVIZ_DOT") == null) {
                OptionFlags.getInstance().setDotExecutable(StringUtils.eventuallyRemoveStartingAndEndingDoubleQuote(ExternalPrograms.getGraphVizDotPath()));
            }
            String desc = reader.generateImage(png);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
            this.setImage(image);
            if (resizeRegion && image != null) {
                this.getActiveRegionContext().setProperty("width", "" + image.getWidth());
                this.getActiveRegionContext().setProperty("height", "" + image.getHeight());
                bScale = false;
            } else {
                bScale = true;
            }
            timeout = false;
        } catch (Exception e) {
            e.printStackTrace();
            timeout = true;
        }
    }

    public static void main(String args[]) throws Exception {
        OutputStream png = new ByteArrayOutputStream();
        String source = "@startuml\n";
        source += "Bob -> Alice : hello\n";
        source += "@enduml\n";

        SourceStringReader reader = new SourceStringReader(source);
        String desc = reader.generateImage(png);
    }
}
