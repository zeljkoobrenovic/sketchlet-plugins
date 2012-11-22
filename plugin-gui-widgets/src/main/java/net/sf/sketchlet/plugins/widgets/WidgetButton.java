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
 *
 * @author zobrenovic
 */
@PluginInfo(name = "Button", type = "widget", group="GUI Controls")
@WidgetPluginEvents(events = {WidgetButton.EVENT_CLICK, WidgetButton.EVENT_PRESS, WidgetButton.EVENT_RELEASE})
public class WidgetButton extends WidgetPlugin {

    public final static String EVENT_CLICK = "click";
    public final static String EVENT_PRESS = "press";
    public final static String EVENT_RELEASE = "release";
    //
    @WidgetPluginProperty(name = "update variable", initValue = "button", description = "[out] A variable updated on press and release")
    private String updateVariable = "";
    //
    @WidgetPluginProperty(name = "value on pressed", initValue = "pressed", description = "A variable value on click")
    private String valueOnPressed = "";
    //
    @WidgetPluginProperty(name = "value on released", initValue = "released", description = "A variable value on click")
    private String valueOnReleased = "";
    //
    @WidgetPluginProperty(name = "caption", initValue = "Button", description = "A caption of the button")
    private String caption = "";
    //
    private boolean pressed = false;

    public WidgetButton(final ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void paint(Graphics2D g2) {
        int w = this.getWidth();
        int h = this.getHeight();
        int x = 0;
        int y = 0;

        g2.setStroke(getActiveRegionContext().getStroke());
        Color c = getActiveRegionContext().getLineColor();


        if (pressed) {
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
            g2.fillRect(x, y, w, h);
        }
        g2.setColor(c);
        g2.drawRect(x, y, w, h);

        g2.setFont(getActiveRegionContext().getFont(this.getHeight() / 2.0f));
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        String text = caption;
        LineMetrics metrics = font.getLineMetrics(text, frc);

        String original = text;
        float textWidth = (float) font.getStringBounds(text, frc).getMaxX();

        while (textWidth > w && original.length() > 0) {
            original = original.substring(0, original.length() - 1);
            text = original + "..";
            textWidth = (float) font.getStringBounds(text, frc).getMaxX();
        }

        g2.drawString(text, x + w / 2 - textWidth / 2, y + metrics.getHeight());


    }

    @Override
    public void mousePressed(MouseEvent me) {
        pressed = true;
        this.updateSketchletVariable(updateVariable, valueOnPressed);
        this.getActiveRegionContext().processEvent(EVENT_PRESS);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        pressed = false;
        this.updateSketchletVariable(updateVariable, valueOnReleased);
        this.getActiveRegionContext().processEvent(EVENT_RELEASE);
        this.getActiveRegionContext().processEvent(EVENT_CLICK);
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            this.pressed = true;
            this.getActiveRegionContext().processEvent(EVENT_PRESS);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            this.pressed = false;
            this.getActiveRegionContext().processEvent("released");
            this.getActiveRegionContext().processEvent(EVENT_CLICK);
        }
    }
}
