/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.ImageCachingWidgetPlugin;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;
import ptolemy.plot.PlotApplication;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Ptplot", type = "widget", group = "Graphs")
@WidgetPluginTextItems(initValue = "TitleText: Software Downloads\n" +
        "XRange: 0,10\n" +
        "YRange: 0,10000\n" +
        "# Manually specify X ticks\n" +
        "# Note that the 0 and 10 point result in clipping of the rectangles.\n" +
        "XTicks: 1993 0, 1994 1, 1995 2, 1996 3, 1997 4, 1998 5, 1999 6, 2000 7, 2001 8, 2002 9, 2003 10\n" +
        "XLabel: Year\n" +
        "YLabel: Downloads\n" +
        "Marks: none\n" +
        "Lines: off\n" +
        "# Width and offset of bars\n" +
        "Bars: 0.5, 0.2\n" +
        "NumSets: 3\n" +
        "\n" +
        "DataSet: program a\n" +
        "0, 100\n" +
        "1, 300\n" +
        "2, 600\n" +
        "3, 1000\n" +
        "4, 4000\n" +
        "5, 6000\n" +
        "6, 3000\n" +
        "7, 1000\n" +
        "8, 400\n" +
        "9, 0\n" +
        "10, 0\n" +
        "\n" +
        "DataSet: program b\n" +
        "0, 0\n" +
        "1, 0\n" +
        "2, 50\n" +
        "3, 100\n" +
        "4, 800\n" +
        "5, 400\n" +
        "6, 1000\n" +
        "7, 5000\n" +
        "8, 2000\n" +
        "9, 300\n" +
        "10, 0\n" +
        "\n" +
        "DataSet: program c\n" +
        "0, 0\n" +
        "1, 0\n" +
        "2, 0\n" +
        "3, 10\n" +
        "4, 100\n" +
        "5, 400\n" +
        "6, 2000\n" +
        "7, 5000\n" +
        "8, 9000\n" +
        "9, 7000\n" +
        "10, 1000\n")

@WidgetPluginLinks(link = "http://ptolemy.berkeley.edu/java/ptplot/")
public class PlotML extends ImageCachingWidgetPlugin {

    private String prevCacheKey = "";

    public PlotML(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void paintImage(Graphics2D g2) {
        String chartSpec = getActiveRegionContext().getWidgetItemText().trim();

        if (!chartSpec.isEmpty()) {
            try {
                chartSpec = VariablesBlackboardContext.getInstance().populateTemplate(chartSpec);

                int w = getActiveRegionContext().getWidth();
                int h = getActiveRegionContext().getHeight();

                BufferedImage image = PlotApplication.getImage(chartSpec, w, h);
                if (image != null) {
                    g2.drawImage(image, 0, 0, null);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean isRegionChanged() {
        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        widgetItemText = VariablesBlackboardContext.getInstance().populateTemplate(widgetItemText.trim());
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();

        widgetItemText += ";" + w + ";" + h;

        if (!widgetItemText.equals(prevCacheKey)) {
            prevCacheKey = widgetItemText;
            return true;
        }
        return super.isRegionChanged();
    }
}
