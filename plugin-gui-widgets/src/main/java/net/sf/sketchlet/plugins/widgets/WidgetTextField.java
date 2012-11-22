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
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "Text Field", type="widget", group="GUI Controls")
@WidgetPluginProperties(properties = {
    "update variable|textfield|[in/out] A variable updated"})
public class WidgetTextField extends WidgetPlugin {
    public static final String UPDATE_VARIABLE_PROPERTY = "update variable";

    private String value = "";
    private long lastTime = System.currentTimeMillis();
    private boolean drawTickEnabled = true;

    public WidgetTextField(final ActiveRegionContext region) {
        super(region);
        String updateVariableName = getActiveRegionContext().getWidgetProperty(UPDATE_VARIABLE_PROPERTY);
        if (!updateVariableName.isEmpty()) {
            this.variableUpdated(updateVariableName, VariablesBlackboardContext.getInstance().getVariableValue(updateVariableName));
        }
        setActiveWidget(this);
    }

    @Override
    public void paint(Graphics2D g2) {
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();

        int x = 0;
        int y = 0;

        g2.setStroke(getActiveRegionContext().getStroke());
        Color c = getActiveRegionContext().getLineColor();
        if (hasFocus()) {
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 20));
            g2.fillRect(x, y, w, h);
        }
        g2.setColor(c);
        g2.drawRect(x, y, w, h);

        g2.setFont(getActiveRegionContext().getFont(getActiveRegionContext().getHeight() / 2.0f));
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        LineMetrics metrics = font.getLineMetrics(value, frc);

        float textWidth = (float) font.getStringBounds(value, frc).getMaxX();
        if (textWidth > w - 5) {
            g2.drawString(value, x + w - textWidth, y + metrics.getHeight());
        } else {
            g2.drawString(value, x + 4, y + metrics.getHeight());
        }

        drawTick(g2, x, y, w, h, (int) textWidth);
    }

    private void drawTick(Graphics2D g2, int x, int y, int w, int h, int textWidth) {
        if (hasFocus()) {
            if (drawTickEnabled) {
                if (textWidth > w - 5) {
                    g2.drawLine(x + w - 3, y + 2, x + w - 3, y + h - 4);
                } else {
                    g2.drawLine((int) (x + textWidth + 6), y + 2, (int) (x + textWidth + 6), y + h - 4);
                }
            }

            if (System.currentTimeMillis() - lastTime > 500) {
                lastTime = System.currentTimeMillis();
                drawTickEnabled = !drawTickEnabled;
            }

            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(500);
                        repaint();
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (value.length() > 0) {
                value = value.substring(0, value.length() - 1);
                updateVariable();
            }
        } else {
            if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                value += e.getKeyChar();
                updateVariable();
            }
        }
        repaint();
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (getActiveRegionContext().getWidgetProperty(UPDATE_VARIABLE_PROPERTY).equalsIgnoreCase(triggerVariable)) {
            this.value = value;
            repaint();
        }
    }

    private void updateVariable() {
        if (!getActiveRegionContext().getWidgetProperty(UPDATE_VARIABLE_PROPERTY).isEmpty()) {
            VariablesBlackboardContext.getInstance().updateVariableIfDifferent(getActiveRegionContext().getWidgetProperty(UPDATE_VARIABLE_PROPERTY), value);
        }
    }
}
