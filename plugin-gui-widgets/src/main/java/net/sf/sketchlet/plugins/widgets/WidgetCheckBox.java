/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.plugin.WidgetPluginEvents;
import net.sf.sketchlet.plugin.WidgetPluginProperty;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Check Box", type = "widget", group = "GUI Controls")
@WidgetPluginEvents(events = {WidgetCheckBox.EVENT_SELECT, WidgetCheckBox.EVENT_DESELECT})
public class WidgetCheckBox extends WidgetPlugin {
    public final static String EVENT_SELECT = "select";
    public final static String EVENT_DESELECT = "deselect";

    @WidgetPluginProperty(name = "update variable", initValue = "checkbox", description = "[in/out] A variable updated on click")
    private String updateVariable = "";

    @WidgetPluginProperty(name = "value selected", initValue = "true", description = "A variable updated on click")
    private String selectedValue = "";

    @WidgetPluginProperty(name = "value unselected", initValue = "false", description = "A variable updated on click")
    private String unselectedValue = "";

    @WidgetPluginProperty(name = "caption", initValue = "Check Box", description = "A caption of the check box")
    private String caption;

    private boolean selected = false;

    public WidgetCheckBox(final ActiveRegionContext region) {
        super(region);

        if (!this.unselectedValue.isEmpty()) {
            this.variableUpdated(updateVariable, getSketchletVariable(updateVariable));
        }
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

        g2.setFont(getActiveRegionContext().getFont(getActiveRegionContext().getHeight() / 2.0f));
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        String text = caption;
        LineMetrics metrics = font.getLineMetrics(text, frc);

        FontMetrics fm = g2.getFontMetrics();
        String original = text;
        float textWidth = (float) font.getStringBounds(text, frc).getMaxX();
        while (textWidth > w && original.length() > 0) {
            original = original.substring(0, original.length() - 1);
            text = original + "...";
            textWidth = (float) font.getStringBounds(text, frc).getMaxX();
        }

        g2.drawRect(x + h / 4, y + h / 4, h / 2, h / 2);
        if (selected) {
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 2));
            g2.drawLine(x + h / 4 + 2, y + h / 4 + 2, x + h - h / 4 - 4, y + h - h / 4 - 4);
            g2.drawLine(x + h / 4 + 2, y + h - h / 4 - 2, x + h - h / 4 - 4, y + h / 4 + 2);
            g2.setColor(c);
        }
        g2.drawString(text, x + h + 5, y + metrics.getHeight());
    }

    @Override
    public void mousePressed(MouseEvent me) {
        selected = !selected;
        this.updateSketchletVariable(updateVariable, selected ? selectedValue : unselectedValue);
        sendAction();
        repaint();
    }

    @Override
    public void variableUpdated(String variable, String value) {
        if (updateVariable.equalsIgnoreCase(variable)) {
            selected = value.equalsIgnoreCase(selectedValue);
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            mousePressed(new MouseEvent(e.getComponent(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 1, 1, 1, false, MouseEvent.BUTTON1));
        }
    }

    private void sendAction() {
        if (selected) {
            this.getActiveRegionContext().processEvent(EVENT_SELECT);
        } else {
            this.getActiveRegionContext().processEvent(EVENT_DESELECT);
        }
    }
}
