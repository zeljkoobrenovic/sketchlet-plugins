/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.common.html.HTMLImageRenderer;
import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.ImageCachingWidgetPlugin;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginProperties;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "HTML Renderer", type="widget", group="HTML, SVG")
@WidgetPluginTextItems(initValue = "<b>Hello</b> <i>There</i>")
@WidgetPluginProperties(properties = {"default font|Bradley Hand ITC|Default text font"})
public class WidgetHTML extends ImageCachingWidgetPlugin {

    private String strPrevInfo = "";

    public WidgetHTML(ActiveRegionContext regionContext) {
        super(regionContext);
    }

    @Override
    public void paintImage(Graphics2D g2) {
        String strText = getActiveRegionContext().getWidgetItemText();
        if (strText.isEmpty()) {
            return;
        }
        String strHtml = VariablesBlackboardContext.getInstance().populateTemplate(strText.trim());
        String strFont = getActiveRegionContext().getWidgetProperty("default font");
        if (strFont != null && !strFont.isEmpty()) {
            strHtml = "<div style=\"font-family:" + strFont + "\">" + strHtml + "</div>";
        }
        if (strHtml.length() > 0) {
            int x1 = 0;
            int y1 = 0;
            int w = getActiveRegionContext().getWidth();
            int h = getActiveRegionContext().getHeight();

            BufferedImage image = getHTMLImage(strHtml, w, h);
            g2.drawImage(image, x1, y1, w, h, null, null);
        }
    }

    @Override
    public boolean isRegionChanged() {
        String strText = getActiveRegionContext().getWidgetItemText();
        String strHtml = VariablesBlackboardContext.getInstance().populateTemplate(strText.trim());
        String strFont = getActiveRegionContext().getWidgetProperty("default font");
        String strInfo = strHtml + strFont;
        if (!strInfo.equals(strPrevInfo)) {
            strPrevInfo = strInfo;
            return true;
        }
        return super.isRegionChanged();
    }

    private BufferedImage getHTMLImage(String strHTML, int w, int h) {
        BufferedImage image = null;
        try {
            image = HTMLImageRenderer.getImage(strHTML, w, h);
        } catch (Throwable e) {
            image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h, image);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.RED);
            g2.drawString("HTML ERROR: ", 10, 30);
            g2.drawString("  " + e.getMessage(), 10, 50);
            g2.dispose();
            e.printStackTrace();
        }
        return image;
    }
}
