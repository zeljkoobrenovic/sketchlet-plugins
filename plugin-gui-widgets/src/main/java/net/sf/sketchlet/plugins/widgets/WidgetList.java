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

    int selectedRow = -1;
    boolean bRadio = false;

    public WidgetList(final ActiveRegionContext region, boolean bRadio) {
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
                {"item position variable", prefix + "_pos", "[in/out] A variable updated with a position of a selected item"},
                {"visible items", "all", "[in/out] A number of visible items"},
                {"start item", "1", "[in/out] First visible item"},};
    }

    public int getItemCount() {
        String strItemCount = getActiveRegionContext().getWidgetProperty("visible items");

        if (strItemCount == null || strItemCount.isEmpty() || strItemCount.equalsIgnoreCase("all")) {
            return -1;
        } else {
            try {
                return (int) Double.parseDouble(strItemCount);
            } catch (Exception e) {
            }
        }

        return -1;
    }

    public int getStartItemIndex() {
        String strStart = getActiveRegionContext().getWidgetProperty("start item");

        if (strStart == null || strStart.isEmpty()) {
            return 0;
        } else {
            try {
                int s = (int) Double.parseDouble(strStart) - 1;
                return s >= 0 ? s : 0;
            } catch (Exception e) {
            }
        }

        return 0;
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

        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        String strText = getActiveRegionContext().getWidgetItemText();
        String rows[] = strText.split("\n");

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
                float textWidth = (float) font.getStringBounds(line, frc).getMaxX();
                while (textWidth > w && original.length() > 0) {
                    original = original.substring(0, original.length() - 1);
                    line = original + "..";
                    textWidth = (float) font.getStringBounds(line, frc).getMaxX();
                }

                if (this.selectedRow == i + start) {
                    if (!bRadio) {
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
                    if (!bRadio) {
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
        String strText = getActiveRegionContext().getWidgetItemText();
        String rows[] = strText.split("\n");

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
        if (getActiveRegionContext().getWidgetProperty("item text variable").equalsIgnoreCase(triggerVariable)) {
            String strText = getActiveRegionContext().getWidgetItemText();
            String lines[] = strText.split("\n");
            this.selectedRow = -1;
            for (int i = 0; i < lines.length; i++) {
                if (ListUtils.getLineText(lines[i]).equalsIgnoreCase(value)) {
                    selectedRow = i;
                    return;
                }
            }
            repaint();
        } else if (getActiveRegionContext().getWidgetProperty("item position variable").equalsIgnoreCase(triggerVariable)) {
            try {
                this.selectedRow = (int) Double.parseDouble(value) - 1;
            } catch (Exception e) {
            }
            repaint();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            String strText = getActiveRegionContext().getWidgetItemText();
            String lines[] = strText.split("\n");

            if (selectedRow < lines.length - 1) {
                selectedRow++;
                updateVariables(lines, selectedRow);
                repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (selectedRow > 0) {
                String strText = getActiveRegionContext().getWidgetItemText();
                String lines[] = strText.split("\n");
                selectedRow--;
                updateVariables(lines, selectedRow);
                repaint();
            }
        }
    }

    public void updateVariables(String lines[], int pos) {
        boolean changed = false;
        String itemText = "";
        String line = lines[pos].trim();
        if (pos >= 0 && pos < lines.length && !this.getActiveRegionContext().getWidgetProperty("item text variable").isEmpty()) {
            changed = true;
            itemText = line;
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("item text variable"), ListUtils.getLineText(line));
        }
        ListUtils.executeCommandIfDefined(line);
        if (!getActiveRegionContext().getWidgetProperty("item position variable").isEmpty()) {
            changed = true;
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("item position variable"), Integer.toString(pos + 1));
        }

        if (changed) {
            this.getActiveRegionContext().processEvent(WidgetHorizontalList.UPDATE_EVENT, Integer.toString(pos + 1), itemText);
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}
