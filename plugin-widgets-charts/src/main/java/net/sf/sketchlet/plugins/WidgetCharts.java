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
import org.jfree.chart.JFreeChart;
import org.jfree.eastwood.ChartEngine;
import org.jfree.eastwood.Parameters;

import java.awt.*;
import java.util.Map;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Google Charts", type = "widget", group = "Graphs")
@WidgetPluginTextItems(initValue = "cht=s\n" +
        "chd=t:12,87,75,41,23,96,68,71,34,9|98,60,27,34,56,79,58,74,18,76|84,23,69,81,47,94,60,93,64,54\n" +
        "chxt=x,y\n" + "chxl=0:|0|20|30|40|50|60|70|80|90|10|1:|0|25|50|75|100")
@WidgetPluginLinks(link = "https://developers.google.com/chart/image/docs/making_charts")
public class WidgetCharts extends ImageCachingWidgetPlugin {

    private String lastCacheKey = "";

    public WidgetCharts(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void paintImage(Graphics2D g2) {
        String chartSpec = getActiveRegionContext().getWidgetItemText().trim();

        if (!chartSpec.isEmpty()) {
            try {
                chartSpec = chartSpec.replace("\r\n", "&");
                chartSpec = chartSpec.replace("\n", "&");
                chartSpec = chartSpec.replace("\r", "&");
                chartSpec = VariablesBlackboardContext.getInstance().populateTemplate(chartSpec);
                chartSpec = chartSpec.replace("&&", "&");
                if (chartSpec.indexOf("?") >= 0) {
                    chartSpec = chartSpec.substring(chartSpec.indexOf("?") + 1);
                }
                Map params = Parameters.parseQueryString(chartSpec);
                JFreeChart chart = ChartEngine.buildChart(params, new Font("Dialog", Font.PLAIN, 14));

                chart.setBackgroundImageAlpha(0.0f);
                chart.setBackgroundPaint(new Color(0, 0, 0, 0));

                int x = 0;
                int y = 0;
                int w = getActiveRegionContext().getWidth();
                int h = getActiveRegionContext().getHeight();

                chart.draw(g2, new Rectangle(x, y, w, h));

            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean isRegionChanged() {
        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        widgetItemText = VariablesBlackboardContext.getInstance().populateTemplate(widgetItemText.trim());
        if (!widgetItemText.equals(lastCacheKey)) {
            lastCacheKey = widgetItemText;
            return true;
        }
        return super.isRegionChanged();
    }
}
