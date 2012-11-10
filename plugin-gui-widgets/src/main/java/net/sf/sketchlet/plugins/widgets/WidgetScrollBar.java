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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "Scroll Bar", type="widget", group="GUI Controls")
@WidgetPluginProperties(properties = {
    "update variable|scrollbar|[in/out] A variable updated",
    "min value|0|[in/out] Minimal value",
    "max value|1|[in/out] Maximal value"})
public class WidgetScrollBar extends WidgetPlugin {

    double relativePosition = 0.0;
    int startX;
    int startY;
    int dX;

    public WidgetScrollBar(final ActiveRegionContext region) {
        super(region);
        String strControlVariable = getActiveRegionContext().getWidgetProperty("update variable");
        if (!strControlVariable.isEmpty()) {
            this.variableUpdated(strControlVariable, VariablesBlackboardContext.getInstance().getVariableValue(strControlVariable));
        }
    }
    int selectedRegion = -1;
    double min = 0.0;
    double max = 1.0;

    private void calculateMinMax() {
        try {
            min = Double.parseDouble(getActiveRegionContext().getWidgetProperty("min value"));
        } catch (Exception e) {
        }

        try {
            max = Double.parseDouble(getActiveRegionContext().getWidgetProperty("max value"));
        } catch (Exception e) {
        }
    }

    private double getAbsolute(double relValue) {
        calculateMinMax();

        if (this.min != this.max) {
            return this.min + (this.max - this.min) * relValue;
        }

        return relValue;
    }

    private double getRelative(double value) {
        calculateMinMax();

        if (this.min != this.max) {
            return (value - this.min) / (this.max - this.min);
        }

        return value;
    }

    private void setPosition(double pos) {
        if (pos < 0) {
            pos = 0;
        } else if (pos > 1) {
            pos = 1;
        }

        this.relativePosition = pos;

        pos = this.getAbsolute(pos);

        DecimalFormat df = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
        String strPos = df.format(pos);

        if (!getActiveRegionContext().getWidgetProperty("update variable").isEmpty()) {
            VariablesBlackboardContext.getInstance().updateVariableIfDifferent(getActiveRegionContext().getWidgetProperty("update variable"), strPos);
        }
    }

    private int getScrollbarRegion(int x, int y) {
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();

        if (y >= 0 && y <= h && x >= 0 && x <= w) {
            if (x <= h) {
                return 1;
            } else if (x >= w - h) {
                return 2;
            } else {
                int len = w - 3 * h;

                if (len > 0 && x >= h + len * relativePosition && x <= 2 * h + len * relativePosition) {
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
        int x = 0;
        int y = 0;

        g2.setStroke(getActiveRegionContext().getStroke());
        Color c = getActiveRegionContext().getLineColor();
        g2.setColor(c);
        g2.drawRect(x, y, w, h);
        g2.drawRect(x, y, h, h);
        g2.drawRect(x, y, w - h, h);

        int len = w - 3 * h;

        if (len > 0) {
            g2.drawRect(x + (int) (h + 3 + len * relativePosition), y + 3, h - 6, h - 6);
        }

        int dw = h / 5;
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));

        if (selectedRegion == 1) {
            g2.fillRect(x, y, h, h);
        }
        if (selectedRegion == 2) {
            g2.fillRect(x + w - h, y, h, h);
        }
        if (selectedRegion == 3) {
            g2.fillRect(x + (int) (h + 3 + len * relativePosition), y + 3, h - 6, h - 6);
        }
        g2.setColor(c);
        g2.drawLine(x + dw, y + h / 2, x + h - dw, y + dw);
        g2.drawLine(x + dw, y + h / 2, x + h - dw, y + h - dw);
        g2.drawLine(x + h - dw, y + dw, x + h - dw, y + h - dw);

        g2.drawLine(x + w - dw, y + h / 2, x + w - h + dw, y + dw);
        g2.drawLine(x + w - dw, y + h / 2, x + w - h + dw, y + h - dw);
        g2.drawLine(x + w - h + dw, y + dw, x + w - h + dw, y + h - dw);
    }

    @Override
    public void mousePressed(MouseEvent me) {
        int x = me.getX();
        int y = me.getY();
        startX = x;
        startY = y;
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();
        int len = w - 3 * h;
        dX = (int) (x - (h + len * relativePosition));
        selectedRegion = getScrollbarRegion(x, y);

        if (selectedRegion == 1) {
            setPosition(relativePosition - 0.1);
        } else if (selectedRegion == 2) {
            setPosition(relativePosition + 0.1);
        }

        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        selectedRegion = -1;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (selectedRegion == 3) {
            int x = me.getX();
            int w = getActiveRegionContext().getWidth();
            int h = getActiveRegionContext().getHeight();
            int len = w - 3 * h;

            setPosition((x - h - h / 2) / (double) len);
            repaint();
        }
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (selectedRegion == -1 && getActiveRegionContext().getWidgetProperty("update variable").equalsIgnoreCase(triggerVariable)) {
            try {
                this.setPosition(this.getRelative(Double.parseDouble(value)));
                repaint();
            } catch (Exception e) {
            }
        }
    }
}
