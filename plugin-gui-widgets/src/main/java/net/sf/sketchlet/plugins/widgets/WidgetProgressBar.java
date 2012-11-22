/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.plugin.WidgetPluginProperties;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Progress Bar", type = "widget", group = "GUI Controls")
@WidgetPluginProperties(properties = {
        "variable link|progress|[in] A variable updated"})
public class WidgetProgressBar extends WidgetPlugin {

    public static final String VARIABLE_LINK_PROPERTY = "variable link";
    private double position = 0.0;
    private int startX;
    private int startY;
    private int dX;
    private int selectedRegion = -1;

    public WidgetProgressBar(final ActiveRegionContext region) {
        super(region);
        String linkVariableName = getActiveRegionContext().getWidgetProperty(VARIABLE_LINK_PROPERTY);
        if (!linkVariableName.isEmpty()) {
            this.variableUpdated(linkVariableName, VariablesBlackboardContext.getInstance().getVariableValue(linkVariableName));
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();
        int x = getActiveRegionContext().getX1();
        int y = getActiveRegionContext().getY1();

        g2.setStroke(getActiveRegionContext().getStroke());
        Color c = getActiveRegionContext().getLineColor();
        g2.setColor(c);
        g2.drawRect(x, y, (int) (w * position), h);
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
        g2.fillRect(x, y, (int) (w * position), h);
        paintLabel(g2);
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (selectedRegion == -1 && getActiveRegionContext().getWidgetProperty(VARIABLE_LINK_PROPERTY).equalsIgnoreCase(triggerVariable)) {
            try {
                this.setPosition(Double.parseDouble(value));
                repaint();
            } catch (Exception e) {
            }
        }
    }

    private void paintLabel(Graphics2D g2) {
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();

        int x = 0;
        int y = 0;

        g2.setStroke(getActiveRegionContext().getStroke());
        Color c = getActiveRegionContext().getLineColor();
        g2.setColor(c);
        g2.drawRect(x, y, w, h);

        g2.setFont(getActiveRegionContext().getFont(getActiveRegionContext().getHeight() / 2.0f));
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        LineMetrics fontLineMetrics = font.getLineMetrics(widgetItemText, frc);

        String original = widgetItemText;
        float textWidth = (float) font.getStringBounds(widgetItemText, frc).getMaxX();
        while (textWidth > w && original.length() > 0) {
            original = original.substring(0, original.length() - 1);
            widgetItemText = original + "...";
            textWidth = (float) font.getStringBounds(widgetItemText, frc).getMaxX();
        }

        g2.drawString(widgetItemText, x + w / 2 - textWidth / 2, y + fontLineMetrics.getHeight());
    }

    private void setPosition(double pos) {
        if (pos < 0) {
            pos = 0;
        } else if (pos > 1) {
            pos = 1;
        }

        position = pos;
    }
}
