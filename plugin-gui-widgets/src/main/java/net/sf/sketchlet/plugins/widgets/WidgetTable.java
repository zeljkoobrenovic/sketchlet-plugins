package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.plugin.WidgetPluginProperties;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "Table", type="widget", group="GUI Controls")
@WidgetPluginTextItems(initValue = "Item 1\tItem1\nItem 2\tItem2\nItem 3\tItem3")
@WidgetPluginProperties(properties = {
    "variable text item|table|[in/out] A variable updated",
    "variable row|row|[in/out] A variable updated with the current  row",
    "variable col|column|[in/out] A variable updated with the current column",
    "visible items|all|[in/out] A number of visible items",
    "start item|1|[in/out] First visible item"})
public class WidgetTable extends WidgetPlugin {

    int selectedRow = -1;
    int selectedColumn = -1;

    public WidgetTable(final ActiveRegionContext region) {
        super(region);
        String strWidgetVariable = getActiveRegionContext().getWidgetProperty("variable text item");
        if (!strWidgetVariable.isEmpty()) {
            this.variableUpdated(strWidgetVariable, VariablesBlackboardContext.getInstance().getVariableValue(strWidgetVariable));
        }
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

        int cols = Math.max(1, this.getColumnCount(rows));

        int count = this.getItemCount();
        if (count == -1) {
            count = rows.length;
        }
        int start = this.getStartItemIndex();
        if (start > rows.length) {
            start = 0;
        }

        try {
            if (count > 0) {
                int lineh = getActiveRegionContext().getHeight() / count;
                g2.setFont(getActiveRegionContext().getFont(lineh));
                double columnWidth = getActiveRegionContext().getWidth() / cols;

                for (int i = 0; i < Math.min(count, rows.length); i++) {
                    String line = i + start < rows.length ? ListUtils.getLineText(rows[i + start]) : "";

                    FontMetrics fm = g2.getFontMetrics();
                    String rowData[] = line.split("\t");
                    for (int ci = 0; ci < rowData.length; ci++) {
                        String strCellText = ci < rowData.length ? rowData[ci] : "";
                        String text = strCellText;

                        float textWidth = (float) font.getStringBounds(text, frc).getMaxX();
                        while (textWidth > columnWidth && strCellText.length() > 0) {
                            strCellText = strCellText.substring(0, strCellText.length() - 1);
                            text = strCellText + "..";
                            textWidth = (float) font.getStringBounds(text, frc).getMaxX();
                        }

                        if (this.selectedRow == i + start) {
                            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 5));
                            g2.fillRect(x, y + lineh * i, w, lineh);
                            g2.setColor(c);
                        }

                        g2.drawString(text, (int) (x + 5 + columnWidth * ci), (int) (y + lineh * (i + 1) - fm.getDescent() / 2));
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public int getColumnCount() {
        String strText = getActiveRegionContext().getWidgetItemText();
        String rows[] = strText.split("\n");

        return this.getColumnCount(rows);
    }

    public int getColumnCount(String rows[]) {
        int cols = 1;

        for (String row : rows) {
            int n = row.split("\t").length;
            if (n > cols) {
                cols = n;
            }
        }

        return cols;
    }

    @Override
    public void mousePressed(MouseEvent me) {
        int x = me.getX();
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
            if (this.selectedRow >= rows.length) {
                this.selectedRow = rows.length - 1;
            }
            this.selectedColumn = (int) (this.getColumnCount() * ((double) x / getActiveRegionContext().getWidth()));

            updateVariables(rows, selectedRow, selectedColumn);
        }
        repaint();
    }

    public boolean hasTextItems() {
        return true;
    }

    public void updateVariables(String[] lines, int row, int col) {
        if (col < 0) {
            col = 0;
        }
        if (row >= 0 && row < lines.length && !getActiveRegionContext().getWidgetProperty("variable text item").isEmpty()) {
            String cells[] = lines[row].split("\t");
            if (cells.length > col) {
                VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("variable text item"), cells[col]);
            }
        }

        if (!getActiveRegionContext().getWidgetProperty("variable row").isEmpty()) {
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("variable row"), Integer.toString(row + 1));
        }
        if (!getActiveRegionContext().getWidgetProperty("variable col").isEmpty()) {
            VariablesBlackboardContext.getInstance().updateVariable(getActiveRegionContext().getWidgetProperty("variable col"), Integer.toString(col + 1));
        }
        ListUtils.executeCommandIfDefined(lines[row]);
    }

    public String getDescription() {
        return "";
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        int modifiers = me.getModifiers();
        int x = me.getX();
        int y = me.getY();
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (getActiveRegionContext().getWidgetProperty("variable row").equalsIgnoreCase(triggerVariable)) {
            try {
                this.selectedRow = (int) Double.parseDouble(value) - 1;
            } catch (Exception e) {
            }
            repaint();
        } else if (getActiveRegionContext().getWidgetProperty("variable col").equalsIgnoreCase(triggerVariable)) {
            try {
                this.selectedColumn = (int) Double.parseDouble(value) - 1;
            } catch (Exception e) {
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            String strText = getActiveRegionContext().getWidgetItemText();
            String lines[] = strText.split("\n");
            if (selectedRow < lines.length - 1) {
                selectedRow++;
                updateVariables(lines, selectedRow, selectedColumn);
                repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (selectedRow > 0) {
                String strText = getActiveRegionContext().getWidgetItemText();
                String lines[] = strText.split("\n");
                selectedRow--;
                updateVariables(lines, selectedRow, selectedColumn);
                repaint();
            }
        }
    }
}
