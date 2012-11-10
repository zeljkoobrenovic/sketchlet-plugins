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
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "Progress Bar", type="widget", group="GUI Controls")
@WidgetPluginProperties(properties = {
    "variable link|progress|[in] A variable updated"})
public class WidgetProgressBar extends WidgetPlugin {

    double position = 0.0;
    int startX;
    int startY;
    int dX;

    public WidgetProgressBar(final ActiveRegionContext region) {
        super(region);
        String strControlVariable = getActiveRegionContext().getWidgetProperty("variable link");
        if (!strControlVariable.isEmpty()) {
            this.variableUpdated(strControlVariable, VariablesBlackboardContext.getInstance().getVariableValue(strControlVariable));
        }
    }
    int selectedRegion = -1;

    public void setPosition(double pos) {
        if (pos < 0) {
            pos = 0;
        } else if (pos > 1) {
            pos = 1;
        }

        position = pos;
    }

    private int getRegion(int x, int y) {

        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();

        if (y >= 0 && y <= h && x >= 0 && x <= w) {
            if (x <= h) {
                return 1;
            } else if (x >= w - h) {
                return 2;
            } else {
                int len = w - 3 * h;

                if (len > 0 && x >= h + len * position && x <= h + h + len * position) {
                    return 3;
                }
            }
        }

        return -1;
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

        String strText = getActiveRegionContext().getWidgetItemText();
        LineMetrics metrics = font.getLineMetrics(strText, frc);

        FontMetrics fm = g2.getFontMetrics();
        String original = strText;
        float textWidth = (float) font.getStringBounds(strText, frc).getMaxX();
        while (textWidth > w && original.length() > 0) {
            original = original.substring(0, original.length() - 1);
            strText = original + "...";
            textWidth = (float) font.getStringBounds(strText, frc).getMaxX();
        }

        g2.drawString(strText, x + w / 2 - textWidth / 2, y + metrics.getHeight());
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (selectedRegion == -1 && getActiveRegionContext().getWidgetProperty("variable link").equalsIgnoreCase(triggerVariable)) {
            try {
                this.setPosition(Double.parseDouble(value));
                repaint();
            } catch (Exception e) {
            }
        }
    }
}
