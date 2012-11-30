package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginEvents;
import net.sf.sketchlet.plugin.WidgetPluginProperties;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "List", type="widget", group="GUI Controls")
@WidgetPluginProperties(properties = {
    "item text variable|item_text|[in/out] A variable updated with a text of a selected item",
    "item position variable|item_pos|[in/out] A variable updated with a position of a selected item",
    "visible items|all|[in/out] A number of visible items",
    "start item|1|[in/out] First visible item"
})
@WidgetPluginTextItems(initValue = "Item 1\nItem 2\nItem 3")
@WidgetPluginEvents(events = {WidgetHorizontalList.UPDATE_EVENT})
public class WidgetNormalList extends WidgetList {

    public WidgetNormalList(ActiveRegionContext regionContext) {
        super(regionContext, false);
    }
}
