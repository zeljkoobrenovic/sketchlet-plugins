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
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "Numeric Keyboard", type="widget", group="GUI Controls")
@WidgetPluginProperties(properties = {
    "variable digits|num_keyboard|[out] Typed numbers",
    "variable key|num_keyboard_key|[out] Last key"})
public class WidgetNumericKeyboard extends WidgetPlugin {

    boolean bPressed = false;
    String keys[][] = {
        {"clear", "=", "/", "*"},
        {"7", "8", "9", "-"},
        {"4", "5", "6", "+"},
        {"1", "2", "3", "enter"},
        {"0", "", ".", ""},};
    int rowSizes[][] = {
        {1, 1, 1, 1},
        {1, 1, 1, 1},
        {1, 1, 1, 1},
        {1, 1, 1, 1},
        {2, 0, 1, 0},};
    int columnSizes[][] = {
        {1, 1, 1, 1},
        {1, 1, 1, 1},
        {1, 1, 1, 1},
        {1, 1, 1, 2},
        {1, 0, 1, 0},};
    int keyCodes[][] = {
        {KeyEvent.VK_DELETE, KeyEvent.VK_EQUALS, KeyEvent.VK_DIVIDE, KeyEvent.VK_MULTIPLY},
        {KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_MINUS},
        {KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_PLUS},
        {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_ENTER},
        {KeyEvent.VK_0, KeyEvent.VK_0, KeyEvent.VK_PERIOD, KeyEvent.VK_ENTER},};

    public WidgetNumericKeyboard(final ActiveRegionContext region) {
        super(region);
        String strControlVariable = getActiveRegionContext().getWidgetProperty("variable digits");
        if (!strControlVariable.isEmpty()) {
            this.variableUpdated(strControlVariable, VariablesBlackboardContext.getInstance().getVariableValue(strControlVariable));
        }
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (!bPressed && (getActiveRegionContext().getWidgetProperty("variable key")).equalsIgnoreCase(triggerVariable)) {
            for (int i = 0; i < keys.length; i++) {
                for (int j = 0; j < keys[i].length; j++) {
                    if (keys[i][j].equalsIgnoreCase(value)) {
                        selectedKey = keys[i][j];
                        bPressed = true;
                        repaint();
                        new Thread(new Runnable() {

                            public void run() {
                                try {
                                    Thread.sleep(200);
                                    bPressed = false;
                                    repaint();
                                } catch (Exception e) {
                                }
                            }
                        }).start();
                        return;
                    }
                }
            }
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
        // g2.drawRect(x, y, w, h);

        int keyw = w / 4;
        int keyh = h / 5;

        g2.setFont(getActiveRegionContext().getFont(keyh / 2.0f));
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        String strText = getActiveRegionContext().getWidgetItemText();
        LineMetrics metrics = font.getLineMetrics(strText, frc);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                String strKey = keys[j][i];
                if (strKey.isEmpty()) {
                    continue;
                }
                int _x = x + keyw * i;
                int _y = y + keyh * j;
                int _w = keyw * rowSizes[j][i];
                int _h = keyh * columnSizes[j][i];
                float textWidth = (float) font.getStringBounds(strKey, frc).getMaxX();
                if (bPressed && selectedKey.equalsIgnoreCase(strKey)) {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
                    g2.fillRect(_x + 2, _y + 2, _w - 4, _h - 4);
                }
                g2.setColor(c);
                g2.drawRect(_x + 2, _y + 2, _w - 4, _h - 4);
                g2.drawString(strKey, _x + _w / 2 - textWidth / 2, _y + _h / 2 + keyh / 5);
            }
        }

        g2.dispose();
    }

    @Override
    public void mousePressed(MouseEvent me) {
        int modifiers = me.getModifiers();
        int x = me.getX();
        int y = me.getY();
        selectKey(x, y);
        bPressed = true;
        repaint();
    }
    String selectedKey = "";

    void selectKey(int mx, int my) {
        selectedKey = "";
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();

        int x = getActiveRegionContext().getX1();
        int y = getActiveRegionContext().getY1();

        int keyw = w / 4;
        int keyh = h / 5;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                String strKey = keys[j][i];
                if (strKey.isEmpty()) {
                    continue;
                }
                int _x = x + keyw * i;
                int _y = y + keyh * j;
                int _w = keyw * rowSizes[j][i];
                int _h = keyh * columnSizes[j][i];
                if (mx > _x && mx < _x + _w && my > _y && my < _y + _h) {
                    selectedKey = strKey;
                    updateVariable();
                    return;
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        bPressed = false;
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        selectedKey = "";
        for (int i = 0; i < keyCodes.length; i++) {
            for (int j = 0; j < keyCodes[i].length; j++) {
                if (e.getKeyCode() == keyCodes[i][j]) {
                    selectedKey = keys[i][j];
                    updateVariable();
                    bPressed = true;
                    repaint();
                    return;
                }
            }
        }
    }
    boolean bClearNextTime = false;

    @Override
    public void keyReleased(KeyEvent e) {
        this.bPressed = false;
        repaint();
    }

    public void updateVariable() {
        if (!selectedKey.isEmpty()) {
            if (!getActiveRegionContext().getWidgetProperty("variable digits").isEmpty()) {
                VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("variable key"), selectedKey);
                if (selectedKey.equalsIgnoreCase("clear")) {
                    VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("variable digits"), "");
                } else if (selectedKey.equalsIgnoreCase("enter")) {
                    bClearNextTime = true;
                } else {
                    if (bClearNextTime) {
                        bClearNextTime = false;
                        VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("variable digits"), selectedKey);
                    } else {
                        VariablesBlackboardContext.getInstance().appendTextToVariable(getActiveRegionContext().getWidgetProperty("variable digits"), selectedKey);
                    }
                }
            }
        }
    }
}
