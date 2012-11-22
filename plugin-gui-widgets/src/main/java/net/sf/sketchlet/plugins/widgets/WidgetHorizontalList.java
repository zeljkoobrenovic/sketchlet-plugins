package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.WidgetPlugin;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;

/**
 * @author zobrenovic
 */
public class WidgetHorizontalList extends WidgetPlugin {
    public final static String UPDATE_EVENT = "selection changed";

    private int selectedIndex = -1;
    private boolean radio = false;
    private FontRenderContext frc;
    private Font font;
    private boolean processing = false;

    public WidgetHorizontalList(final ActiveRegionContext region, boolean radio) {
        super(region);
        this.radio = radio;
        String controlVariable = getActiveRegionContext().getWidgetProperty("item text variable");
        if (!controlVariable.isEmpty()) {
            this.variableUpdated(controlVariable, VariablesBlackboardContext.getInstance().getVariableValue(controlVariable));
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        int h = getActiveRegionContext().getHeight();

        int x = 0;
        int y = 0;

        g2.setStroke(getActiveRegionContext().getStroke());
        Color c = getActiveRegionContext().getLineColor();
        g2.setColor(c);

        frc = g2.getFontRenderContext();

        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        String items[] = widgetItemText.split("\n");
        try {
            if (items.length > 0) {
                int lineh = h;
                font = getActiveRegionContext().getFont(lineh);
                g2.setFont(font);
                for (int i = 0; i < items.length; i++) {
                    String line = " " + ListUtils.getLineText(items[i]) + " ";

                    FontMetrics fm = g2.getFontMetrics();
                    float textWidth = (float) font.getStringBounds(line, frc).getMaxX();

                    if (this.selectedIndex == i) {
                        if (!radio) {
                            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
                            g2.fillRect(x, y, (int) textWidth, lineh);
                            g2.setColor(c);
                        } else {
                            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 2));
                            g2.fillOval(x + lineh / 4, y + lineh / 4, lineh / 2, lineh / 2);
                            g2.setColor(c);
                        }
                    }

                    if (!radio) {
                        //g2.setColor(cTxt);
                        g2.drawString(line, x, (int) (y + lineh - fm.getDescent() / 2));
                        x += textWidth;
                    } else {
                        g2.drawOval(x + 2, y + 2, lineh - 4, lineh - 4);
                        //g2.setColor(cTxt);
                        g2.drawString(line, x + lineh, (int) (y + lineh - fm.getDescent() / 2));
                        x += textWidth + h;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        int x = me.getX();
        int y = me.getY();
        processing = true;
        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        String items[] = widgetItemText.split("\n");

        if (frc != null && items.length > 0) {
            int _x = 0;
            int lineh = getActiveRegionContext().getHeight();
            font = getActiveRegionContext().getFont(lineh);

            for (int i = 0; i < items.length; i++) {
                String line = " " + ListUtils.getLineText(items[i]) + " ";
                int textWidth = (int) font.getStringBounds(line, frc).getMaxX();
                if (radio) {
                    textWidth += lineh;
                }
                if (x >= _x && x <= _x + textWidth) {
                    selectedIndex = i;
                    break;
                }

                _x += textWidth;
            }

            updateVariables(items, selectedIndex);
        }
        repaint();
        processing = false;
    }

    private void updateVariables(String lines[], int pos) {
        boolean changed = false;
        String itemText = "";
        if (pos >= 0 && pos < lines.length && !this.getActiveRegionContext().getWidgetProperty("item text variable").isEmpty()) {
            changed = true;
            itemText = lines[pos].trim();
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("item text variable"), itemText);
        }
        ListUtils.executeCommandIfDefined(lines[pos].trim());
        if (!getActiveRegionContext().getWidgetProperty("item position variable").isEmpty()) {
            changed = true;
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("item position variable"), Integer.toString(pos + 1));
        }

        if (changed) {
            this.getActiveRegionContext().processEvent(WidgetHorizontalList.UPDATE_EVENT, Integer.toString(pos + 1), itemText);
        }
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (processing) {
            return;
        }
        if (getActiveRegionContext().getWidgetProperty("item text variable").equalsIgnoreCase(triggerVariable)) {
            String widgetItemText = getActiveRegionContext().getWidgetItemText();
            String lines[] = widgetItemText.split("\n");
            this.selectedIndex = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().equalsIgnoreCase(value.trim())) {
                    selectedIndex = i;
                    return;
                }
            }
            repaint();
        } else if (getActiveRegionContext().getWidgetProperty("item position variable").equalsIgnoreCase(triggerVariable)) {
            try {
                this.selectedIndex = (int) Double.parseDouble(value) - 1;
            } catch (Exception e) {
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            String widgetItemText = getActiveRegionContext().getWidgetItemText();
            String lines[] = widgetItemText.split("\n");

            if (selectedIndex < lines.length - 1) {
                selectedIndex++;
                updateVariables(lines, selectedIndex);
                repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (selectedIndex > 0) {
                String widgetItemText = getActiveRegionContext().getWidgetItemText();
                String lines[] = widgetItemText.split("\n");
                selectedIndex--;
                updateVariables(lines, selectedIndex);
                repaint();
            }
        }
    }
}
