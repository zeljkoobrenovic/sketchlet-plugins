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

    int selectedIndex = -1;
    boolean bRadio = false;
    public final static String UPDATE_EVENT = "selection changed";

    public WidgetHorizontalList(final ActiveRegionContext region, boolean bRadio) {
        super(region);
        this.bRadio = bRadio;
        String strControlVariable = getActiveRegionContext().getWidgetProperty("item text variable");
        if (!strControlVariable.isEmpty()) {
            this.variableUpdated(strControlVariable, VariablesBlackboardContext.getInstance().getVariableValue(strControlVariable));
        }
    }

    public String getDefaultItemsText() {
        return "Item 1\nItem 2\nItem 3";
    }

    public String[][] getPropertiesDefaults() {
        String prefix = bRadio ? "radiolist" : "list";
        return new String[][]{
                {"item text variable", prefix + "_item", "[in/out] A variable updated with a text of a selected item"},
                {"item position variable", prefix + "_pos", "[in/out] A variable updated with a position of a selected item"}
        };
    }

    @Override
    public void paint(Graphics2D g2) {
        int h = getActiveRegionContext().getHeight();

        int x = 0;
        int y = 0;

        g2.setStroke(getActiveRegionContext().getStroke());
        Color c = getActiveRegionContext().getLineColor();
        Color cTxt = getActiveRegionContext().getTextColor();
        g2.setColor(c);
        // g2.drawRect(x, y, w, h);

        frc = g2.getFontRenderContext();

        String strText = getActiveRegionContext().getWidgetItemText();
        String items[] = strText.split("\n");
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
                        if (!bRadio) {
                            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
                            g2.fillRect(x, y, (int) textWidth, lineh);
                            g2.setColor(c);
                        } else {
                            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 2));
                            g2.fillOval(x + lineh / 4, y + lineh / 4, lineh / 2, lineh / 2);
                            g2.setColor(c);
                        }
                    }

                    if (!bRadio) {
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

    FontRenderContext frc;
    Font font;
    boolean bProcessing = false;

    @Override
    public void mousePressed(MouseEvent me) {
        int x = me.getX();
        int y = me.getY();
        bProcessing = true;
        String strText = getActiveRegionContext().getWidgetItemText();
        String items[] = strText.split("\n");

        if (frc != null && items.length > 0) {
            int _x = 0;
            int lineh = getActiveRegionContext().getHeight();
            font = getActiveRegionContext().getFont(lineh);

            for (int i = 0; i < items.length; i++) {
                String line = " " + ListUtils.getLineText(items[i]) + " ";
                int textWidth = (int) font.getStringBounds(line, frc).getMaxX();
                if (bRadio) {
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
        bProcessing = false;
    }

    public void updateVariables(String lines[], int pos) {
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

    public boolean hasTextItems() {
        return true;
    }

    public String getDescription() {
        return "";
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    public void variableUpdated(String triggerVariable, String value) {
        if (bProcessing) {
            return;
        }
        if (getActiveRegionContext().getWidgetProperty("item text variable").equalsIgnoreCase(triggerVariable)) {
            String strText = getActiveRegionContext().getWidgetItemText();
            String lines[] = strText.split("\n");
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

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            String strText = getActiveRegionContext().getWidgetItemText();
            String lines[] = strText.split("\n");

            if (selectedIndex < lines.length - 1) {
                selectedIndex++;
                updateVariables(lines, selectedIndex);
                repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (selectedIndex > 0) {
                String strText = getActiveRegionContext().getWidgetItemText();
                String lines[] = strText.split("\n");
                selectedIndex--;
                updateVariables(lines, selectedIndex);
                repaint();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}
