/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.ScriptPluginAutoCompletion;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;
import net.sf.sketchlet.uml.CascadingUmlUtil;

import java.util.*;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Cascading UML", type = "widget", group="UML", position = 251)
@WidgetPluginTextItems(initValue = "A <stereotype1>: Super\n"
        + "B <stereotype2>: Super\n"
        + "Super:\n"
        + "  fields: s1, s2, s3\n"
        + "  operations: op1(), void op2(), int op3(int p)")
public class WidgetCascadingUml extends WidgetUMLGraph implements ScriptPluginAutoCompletion {
    //

    @WidgetPluginProperty(name = "dot parameters", initValue = "",
            description = "Additional parameters to be sent to the dot program",
            valueList = {"-Gratio=0.7 -Eminlen=2", "-Grankdir=LR", "-Grankdir=TB"})
    protected String cmdLineParams = "";
    //
    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size")
    protected boolean resizeRegion = true;
    //
    @WidgetPluginProperty(name = "style", initValue = "normal", description = "Visual style",
            valueList = {"normal", "sketchy"})
    protected String style = "normal";

    public WidgetCascadingUml(ActiveRegionContext region) {
        super(region);
    }

    protected String getText() {
        super.cmdLineParams = this.cmdLineParams;
        super.resizeRegion = this.resizeRegion;
        return getActiveRegionContext().getWidgetItemText();
    }

    @Override
    protected String getUMLGraphCode() {
        String text = this.getText();
        text += "\n";

        if (this.style.equalsIgnoreCase("sketchy")) {
            text += "\n";
            text += "*\n";
            text += "   nodefontname: \"Bradley Hand ITC\"\n";
            text += "   edgefontname: \"Bradley Hand ITC\"\n";
            text += "   nodefontsize:12\n";
        }

        return new CascadingUmlUtil().getUmlGraphCode(text);
    }

    protected String code = "";
    String prevText = "";

    @Override
    public Map<String, List<String>> getAutoCompletionPairs() {
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        CascadingUmlUtil cuml =  new CascadingUmlUtil();
        cuml.getUmlGraphCode(this.getText());
        List<String> allNodes = cuml.getAllNodes();
        List<String> allStereotypes = cuml.getAllStereotypes();
        List<String> allNamespaces = cuml.getAllNamespaces();

        List<String> types = Arrays.asList("abstract class", "class", "node", "note", "component", "package", "collaboration", "usecase", "activeclass", "interface");
        List<String> typesWithSpace = Arrays.asList("abstract class ", "class ", "node ", "note ", "component ", "package ", "collaboration ", "usecase ", "activeclass ", "interface ");
        map.put("type:", types);
        // map.put("\n", typesWithSpace);
        map.put("<", allStereotypes);
        map.put("namespace:", allNamespaces);
        map.put("namespace: ", allNamespaces);

        if (allNodes.size() > 0) {
            map.put(" :", allNodes);
            map.put(" : ", allNodes);
            map.put("depend: ", allNodes);
            map.put("has: ", allNodes);
            map.put("inherits: ", allNodes);
            map.put("specializes: ", allNodes);
            map.put("implements: ", allNodes);
            map.put("assoc: ", allNodes);
            map.put("navassoc: ", allNodes);
            map.put("inheritedby: ", allNodes);
            map.put("composed: ", allNodes);
            map.put("revnavassoc: ", allNodes);
            map.put("revdepends: ", allNodes);
            map.put("partof: ", allNodes);
            map.put("composedby: ", allNodes);
            map.put("depend:", allNodes);
            map.put("has:", allNodes);
            map.put("inherits:", allNodes);
            map.put("specializes:", allNodes);
            map.put("implements:", allNodes);
            map.put("assoc:", allNodes);
            map.put("navassoc:", allNodes);
            map.put("inheritedby:", allNodes);
            map.put("composed:", allNodes);
            map.put("revnavassoc:", allNodes);
            map.put("revdepends:", allNodes);
            map.put("partof:", allNodes);
            map.put("composedby:", allNodes);
            map.put(", ", allNodes);
        }

        String attributes[] = new String[]{
                "depend:",
                "has:",
                "inherits:",
                "type:",
                "stereotype:",
                "text:",
                "id:",
                "namespace:",
                "tag:",
                "attributes:",
                "operations:",
                "show:",
                "specializes:",
                "implements:",
                "assoc:",
                "navassoc:",
                "composed:",
                "inheritedby:",
                "implementedby:",
                "revnavassoc:",
                "revdepends:",
                "partof:",
                "composedby:",
                "note:",
                "color:",
                "fontname:",
                "fontsize:",
                "fontstyle:",
                "opt:"};

        map.put("\n ", Arrays.asList(attributes));
        map.put("  ", Arrays.asList(attributes));

        for (String attr : attributes) {
            String key = "  " + attr.substring(0, 1);
            List<String> list = map.get(key);
            if (list == null) {
                list = new ArrayList<String>();
                map.put(key, list);
            }

            list.add(attr);
        }

        String svgColors[] = new String[]{
                "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure",
                "beige", "bisque", "black", "blanchedalmond", "blue",
                "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse",
                "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson",
                "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray",
                "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen",
                "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen",
                "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet",
                "deeppink", "deepskyblue", "dimgray", "dimgrey", "dodgerblue",
                "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro",
                "ghostwhite", "gold", "goldenrod", "gray", "grey",
                "green", "greenyellow", "honeydew", "hotpink", "indianred",
                "indigo", "ivory", "khaki", "lavender", "lavenderblush",
                "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan",
                "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink",
                "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey",
                "lightsteelblue", "lightyellow", "lime", "limegreen", "linen",
                "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid",
                "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise",
                "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin",
                "navajowhite", "navy", "oldlace", "olive", "olivedrab",
                "orange", "orangered", "orchid", "palegoldenrod", "palegreen",
                "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru",
                "pink", "plum", "powderblue", "purple", "red",
                "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown",
                "seagreen", "seashell", "sienna", "silver", "skyblue",
                "slateblue", "slategray", "slategrey", "snow", "springgreen",
                "steelblue", "tan", "teal", "thistle", "tomato",
                "turquoise", "violet", "wheat", "white", "whitesmoke",
                "yellow", "yellowgreen"};

        map.put("color:", Arrays.asList(svgColors));
        map.put("color: ", Arrays.asList(svgColors));

        String cardinalities[] = new String[] {"[* - *]", "[end1 name end2]", "[end1 \"complex name\" end2]", "[1 - 1]", "[1 - *]", "[* - 1]", "[1..* - *]", "[* - 1..*]", "[0..1 - 1]"};
        map.put("[", Arrays.asList(cardinalities));

        return map;
    }
}
