/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginProperties;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;

/**
 *
 * @author zobrenovic
 */
@PluginInfo(name = "Horizontal Radio List", type="widget", group="GUI Controls")
@WidgetPluginProperties(properties = {
    "item text variable|item_text|[in/out] A variable updated with a text of a selected item",
    "item position variable|item_pos|[in/out] A variable updated with a position of a selected item"
})
@WidgetPluginTextItems(initValue = "Item 1\nItem 2\nItem 3")
public class WidgetRadioHorizontalList extends WidgetHorizontalList {

    public WidgetRadioHorizontalList(ActiveRegionContext regionContext) {
        super(regionContext, true);
    }
}
