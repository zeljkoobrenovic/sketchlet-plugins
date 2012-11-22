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

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "PlantUML", type = "widget", group="UML", position = 259)
@WidgetPluginTextItems(initValue = "@startuml\nBob -> Alice : hello\n@enduml\n")
@WidgetPluginLinks(links = {
        "PlantUML Home Page; http://plantuml.sourceforge.net/"
})
public class WidgetPlantUml extends ExternalImageProgramGeneratorWidget {

    private static final String START_UML_TAG = "@startuml";
    private static final String END_UML_TAG = "@enduml";
    private static final String GRAPHVIZ_DOT_SYSTEM_VARIABLE = "GRAPHVIZ_DOT";

    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size")
    private boolean resizeRegion = true;

    public WidgetPlantUml(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void callImageGenerator() {
        try {
            if (this.getActiveRegionContext() == null) {
                return;
            }
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            String source = this.getActiveRegionContext().getWidgetItemText().trim();

            if (!source.startsWith(START_UML_TAG)) {
                source = START_UML_TAG + "\n" + source;
            }

            if (!source.endsWith(END_UML_TAG)) {
                source += "\n" + END_UML_TAG;
            }
            SourceStringReader reader = new SourceStringReader(source);
            if (System.getenv(GRAPHVIZ_DOT_SYSTEM_VARIABLE) == null) {
                OptionFlags.getInstance().setDotExecutable(StringUtils.eventuallyRemoveStartingAndEndingDoubleQuote(ExternalPrograms.getGraphVizDotPath()));
            }

            reader.generateImage(png);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
            this.setImage(image);
            if (resizeRegion && image != null) {
                this.getActiveRegionContext().setProperty("width", "" + image.getWidth());
                this.getActiveRegionContext().setProperty("height", "" + image.getHeight());
                setScaling(false);
            } else {
                setScaling(true);
            }
            setTimeout(false);
        } catch (Exception e) {
            e.printStackTrace();
            setTimeout(true);
        }
    }
}
