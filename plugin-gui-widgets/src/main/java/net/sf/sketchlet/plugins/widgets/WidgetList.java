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
public class WidgetList extends WidgetPlugin {

    public static final String ITEM_TEXT_VARIABLE_PROPERTY = "item text variable";
    public static final String ITEM_POSITION_VARIABLE_PROPERTY = "item position variable";
    public static final String VISIBLE_ITEMS_PROPERTY = "visible items";
    public static final String START_ITEM_PROPERTY = "start item";

    private int selectedRow = -1;
    private boolean radio = false;

    public WidgetList(final ActiveRegionContext region, boolean radio) {
        super(region);
        this.radio = radio;
        String controlVariable = getActiveRegionContext().getWidgetProperty(ITEM_TEXT_VARIABLE_PROPERTY);
        if (!controlVariable.isEmpty()) {
            this.variableUpdated(controlVariable, VariablesBlackboardContext.getInstance().getVariableValue(controlVariable));
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

        FontRenderContext fontRenderContext = g2.getFontRenderContext();
        Font font = g2.getFont();

        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        String rows[] = widgetItemText.split("\n");

        int count = this.getItemCount();
        if (count == -1) {
            count = rows.length;
        }
        int start = this.getStartItemIndex();
        if (start > rows.length) {
            start = 0;
        }
        if (count > 0) {
            int lineh = getActiveRegionContext().getHeight() / count;
            g2.setFont(getActiveRegionContext().getFont(lineh));
            for (int i = 0; i < Math.min(count, rows.length); i++) {
                String line = i + start < rows.length ? rows[i + start] : "";
                line = ListUtils.getLineText(line);

                FontMetrics fm = g2.getFontMetrics();
                String original = line;
                float textWidth = (float) font.getStringBounds(line, fontRenderContext).getMaxX();
                while (textWidth > w && original.length() > 0) {
                    original = original.substring(0, original.length() - 1);
                    line = original + "..";
                    textWidth = (float) font.getStringBounds(line, fontRenderContext).getMaxX();
                }

                if (this.selectedRow == i + start) {
                    if (!radio) {
                        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
                        g2.fillRect(x, y + lineh * i, w, lineh);
                        g2.setColor(c);
                    } else {
                        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 2));
                        g2.fillOval(x + lineh / 4 + 2, y + lineh * i + lineh / 4 + 2, lineh / 2 - 4, lineh / 2 - 4);
                        g2.setColor(c);
                    }
                }

                if (!line.isEmpty()) {
                    if (!radio) {
                        g2.drawString(line, x + 5, (int) (y + lineh * (i + 1) - fm.getDescent() / 2));
                    } else {
                        g2.drawOval(x + 2, y + lineh * i + 2, lineh - 4, lineh - 4);
                        g2.drawString(line, x + 5 + lineh, (int) (y + lineh * (i + 1) - fm.getDescent() / 2));
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        int y = me.getY();
        String widgetItemText = getActiveRegionContext().getWidgetItemText();
        String rows[] = widgetItemText.split("\n");

        int count = this.getItemCount();
        if (count == -1) {
            count = rows.length;
        }
        int start = this.getStartItemIndex();
        if (start > rows.length) {
            start = 0;
        }

        if (count > 0) {
            int lineh = getActiveRegionContext().getHeight() / count;
            this.selectedRow = start + Math.min(count - 1, y / lineh);

            if (this.selectedRow >= rows.length && rows.length > 0) {
                this.selectedRow = rows.length - 1;
            }

            updateVariables(rows, selectedRow);
        }
        repaint();
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (getActiveRegionContext().getWidgetProperty(ITEM_TEXT_VARIABLE_PROPERTY).equalsIgnoreCase(triggerVariable)) {
            String widgetItemText = getActiveRegionContext().getWidgetItemText();
            String lines[] = widgetItemText.split("\n");
            this.selectedRow = -1;
            for (int i = 0; i < lines.length; i++) {
                if (ListUtils.getLineText(lines[i]).equalsIgnoreCase(value)) {
                    selectedRow = i;
                    return;
                }
            }
            repaint();
        } else if (getActiveRegionContext().getWidgetProperty(ITEM_POSITION_VARIABLE_PROPERTY).equalsIgnoreCase(triggerVariable)) {
            try {
                this.selectedRow = (int) Double.parseDouble(value) - 1;
            } catch (Exception e) {
            }
            repaint();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            String widgetItemText = getActiveRegionContext().getWidgetItemText();
            String lines[] = widgetItemText.split("\n");

            if (selectedRow < lines.length - 1) {
                selectedRow++;
                updateVariables(lines, selectedRow);
                repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (selectedRow > 0) {
                String widgetItemText = getActiveRegionContext().getWidgetItemText();
                String lines[] = widgetItemText.split("\n");
                selectedRow--;
                updateVariables(lines, selectedRow);
                repaint();
            }
        }
    }

    private void updateVariables(String lines[], int pos) {
        boolean changed = false;
        String itemText = "";
        String line = lines[pos].trim();
        if (pos >= 0 && pos < lines.length && !this.getActiveRegionContext().getWidgetProperty(ITEM_TEXT_VARIABLE_PROPERTY).isEmpty()) {
            changed = true;
            itemText = line;
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty(ITEM_TEXT_VARIABLE_PROPERTY), ListUtils.getLineText(line));
        }
        ListUtils.executeCommandIfDefined(line);
        if (!getActiveRegionContext().getWidgetProperty(ITEM_POSITION_VARIABLE_PROPERTY).isEmpty()) {
            changed = true;
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty(ITEM_POSITION_VARIABLE_PROPERTY), Integer.toString(pos + 1));
        }

        if (changed) {
            this.getActiveRegionContext().processEvent(WidgetHorizontalList.UPDATE_EVENT, Integer.toString(pos + 1), itemText);
        }
    }

    private int getItemCount() {
        String itemCountValue = getActiveRegionContext().getWidgetProperty(WidgetList.VISIBLE_ITEMS_PROPERTY);

        if (itemCountValue == null || itemCountValue.isEmpty() || itemCountValue.equalsIgnoreCase("all")) {
            return -1;
        } else {
            try {
                return (int) Double.parseDouble(itemCountValue);
            } catch (Exception e) {
            }
        }

        return -1;
    }

    private int getStartItemIndex() {
        String startIndex = getActiveRegionContext().getWidgetProperty(START_ITEM_PROPERTY);

        if (startIndex == null || startIndex.isEmpty()) {
            return 0;
        } else {
            try {
                int index = (int) Double.parseDouble(startIndex) - 1;
                return index >= 0 ? index : 0;
            } catch (Exception e) {
            }
        }

        return 0;
    }
}
