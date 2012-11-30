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

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Numeric Keyboard", type = "widget", group = "GUI Controls")
@WidgetPluginProperties(properties = {
        "variable digits|num_keyboard|[out] Typed numbers",
        "variable key|num_keyboard_key|[out] Last key"})
public class WidgetNumericKeyboard extends WidgetPlugin {

    public static final String VARIABLE_DIGITS_PROPERTY = "variable digits";

    private boolean pressed = false;
    public static final String CLEAR_KEY = "clear";
    public static final String ENTER_KEY = "enter";
    private static String keys[][] = {
            {CLEAR_KEY, "=", "/", "*"},
            {"7", "8", "9", "-"},
            {"4", "5", "6", "+"},
            {"1", "2", "3", ENTER_KEY},
            {"0", "", ".", ""},};
    private static int rowSizes[][] = {
            {1, 1, 1, 1},
            {1, 1, 1, 1},
            {1, 1, 1, 1},
            {1, 1, 1, 1},
            {2, 0, 1, 0},};
    private static int columnSizes[][] = {
            {1, 1, 1, 1},
            {1, 1, 1, 1},
            {1, 1, 1, 1},
            {1, 1, 1, 2},
            {1, 0, 1, 0},};
    private static int keyCodes[][] = {
            {KeyEvent.VK_DELETE, KeyEvent.VK_EQUALS, KeyEvent.VK_DIVIDE, KeyEvent.VK_MULTIPLY},
            {KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_MINUS},
            {KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_PLUS},
            {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_ENTER},
            {KeyEvent.VK_0, KeyEvent.VK_0, KeyEvent.VK_PERIOD, KeyEvent.VK_ENTER},};
    private String selectedKey = "";
    private boolean clearTextNextTimeFlagSet = false;

    public WidgetNumericKeyboard(final ActiveRegionContext region) {
        super(region);
        String digitsVariableName = getActiveRegionContext().getWidgetProperty(VARIABLE_DIGITS_PROPERTY);
        if (!digitsVariableName.isEmpty()) {
            this.variableUpdated(digitsVariableName, VariablesBlackboardContext.getInstance().getVariableValue(digitsVariableName));
        }
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (!pressed && (getActiveRegionContext().getWidgetProperty("variable key")).equalsIgnoreCase(triggerVariable)) {
            for (int i = 0; i < keys.length; i++) {
                for (int j = 0; j < keys[i].length; j++) {
                    if (keys[i][j].equalsIgnoreCase(value)) {
                        selectedKey = keys[i][j];
                        pressed = true;
                        repaint();
                        new Thread(new Runnable() {

                            public void run() {
                                try {
                                    Thread.sleep(200);
                                    pressed = false;
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

        int keyw = w / 4;
        int keyh = h / 5;

        g2.setFont(getActiveRegionContext().getFont(keyh / 2.0f));
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                String key = keys[j][i];
                if (key.isEmpty()) {
                    continue;
                }
                int _x = x + keyw * i;
                int _y = y + keyh * j;
                int _w = keyw * rowSizes[j][i];
                int _h = keyh * columnSizes[j][i];
                float textWidth = (float) font.getStringBounds(key, frc).getMaxX();
                if (pressed && selectedKey.equalsIgnoreCase(key)) {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
                    g2.fillRect(_x + 2, _y + 2, _w - 4, _h - 4);
                }
                g2.setColor(c);
                g2.drawRect(_x + 2, _y + 2, _w - 4, _h - 4);
                g2.drawString(key, _x + _w / 2 - textWidth / 2, _y + _h / 2 + keyh / 5);
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
        pressed = true;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        pressed = false;
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
                    pressed = true;
                    repaint();
                    return;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        this.pressed = false;
        repaint();
    }

    private void updateVariable() {
        if (!selectedKey.isEmpty()) {
            if (!getActiveRegionContext().getWidgetProperty(VARIABLE_DIGITS_PROPERTY).isEmpty()) {
                VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("variable key"), selectedKey);
                if (selectedKey.equalsIgnoreCase(CLEAR_KEY)) {
                    VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty(VARIABLE_DIGITS_PROPERTY), "");
                } else if (selectedKey.equalsIgnoreCase(ENTER_KEY)) {
                    clearTextNextTimeFlagSet = true;
                } else {
                    if (clearTextNextTimeFlagSet) {
                        clearTextNextTimeFlagSet = false;
                        VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty(VARIABLE_DIGITS_PROPERTY), selectedKey);
                    } else {
                        VariablesBlackboardContext.getInstance().appendTextToVariable(getActiveRegionContext().getWidgetProperty(VARIABLE_DIGITS_PROPERTY), selectedKey);
                    }
                }
            }
        }
    }

    private void selectKey(int mx, int my) {
        selectedKey = "";
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();

        int x = getActiveRegionContext().getX1();
        int y = getActiveRegionContext().getY1();

        int keyWidth = w / 4;
        int keyHeight = h / 5;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                String key = keys[j][i];
                if (key.isEmpty()) {
                    continue;
                }
                int _x = x + keyWidth * i;
                int _y = y + keyHeight * j;
                int _w = keyWidth * rowSizes[j][i];
                int _h = keyHeight * columnSizes[j][i];
                if (mx > _x && mx < _x + _w && my > _y && my < _y + _h) {
                    selectedKey = key;
                    updateVariable();
                    return;
                }
            }
        }
    }
}
