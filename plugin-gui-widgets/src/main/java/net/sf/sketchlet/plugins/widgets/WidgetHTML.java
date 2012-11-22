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

    public static final String DEFAULT_FONT_PROPERTY = "default font";
    private String prevInfo = "";

    public WidgetHTML(ActiveRegionContext regionContext) {
        super(regionContext);
    }

    @Override
    public void paintImage(Graphics2D g2) {
        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        if (widgetItemText.isEmpty()) {
            return;
        }
        String htmlContent = VariablesBlackboardContext.getInstance().populateTemplate(widgetItemText.trim());
        String fontName = getActiveRegionContext().getWidgetProperty(DEFAULT_FONT_PROPERTY);
        if (fontName != null && !fontName.isEmpty()) {
            htmlContent = "<div style=\"font-family:" + fontName + "\">" + htmlContent + "</div>";
        }
        if (htmlContent.length() > 0) {
            int x1 = 0;
            int y1 = 0;
            int w = getActiveRegionContext().getWidth();
            int h = getActiveRegionContext().getHeight();

            BufferedImage image = getHTMLImage(htmlContent, w, h);
            g2.drawImage(image, x1, y1, w, h, null, null);
        }
    }

    @Override
    public boolean isRegionChanged() {
        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        String htmlContent = VariablesBlackboardContext.getInstance().populateTemplate(widgetItemText.trim());
        String font = getActiveRegionContext().getWidgetProperty(DEFAULT_FONT_PROPERTY);
        String cacheKey = htmlContent + font;
        if (!cacheKey.equals(prevInfo)) {
            prevInfo = cacheKey;
            return true;
        }
        return super.isRegionChanged();
    }

    private BufferedImage getHTMLImage(String htmlContent, int w, int h) {
        BufferedImage image = null;
        try {
            image = HTMLImageRenderer.getImage(htmlContent, w, h);
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
