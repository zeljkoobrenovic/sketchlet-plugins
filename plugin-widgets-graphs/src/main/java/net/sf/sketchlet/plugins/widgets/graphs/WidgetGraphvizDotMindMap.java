package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Graphviz Dot / Mind Map", type = "widget", group = "Graphs", position = 252)
@WidgetPluginTextItems(initValue = "ABC\n A\n  A1\n   A1.1\n   A1.2\n B\n  B1\n  B2\n   B2.1\n   B2.1")
@WidgetPluginLinks(links = {
        "Graphviz dot Examples; http://www.graphviz.org/Documentation.php"
})
public class WidgetGraphvizDotMindMap extends WidgetGraphvizDot {

    @WidgetPluginProperty(name = "dot parameters", initValue = "", description = "Additional parameters to be sent to the dot program", valueList = {"-Granksep=0"})
    private String cmdLineParams = "";
    @WidgetPluginProperty(name = "shape", initValue = "note", description = "Shape of the node",
            valueList = {"box", "polygon", "ellipse", "oval", "circle", "point",
                    "egg", "triangle", "plaintext", "diamond", "trapezium", "parallelogram",
                    "house", "pentagon", "hexagon", "septagon", "octagon", "doublecircle",
                    "doubleoctagon", "tripleoctagon", "invtriangle", "invtrapezium", "invhouse",
                    "Mdiamond", "Msquare", "Mcircle", "rect", "rectangle", "square", "tab",
                    "folder", "box3d", "component", "record", "Mrecord"})
    private String shape = "note";
    @WidgetPluginProperty(name = "font size", initValue = "10", description = "Font size")
    private String fontsize = "10";
    @WidgetPluginProperty(name = "font name", initValue = "Helvetica-Outline", description = "The font of the node text")
    private String fontName = "Helvetica-Outline";
    @WidgetPluginProperty(name = "color", initValue = "skyblue", description = "Color of the node",
            valueList = {"antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond", "blue",
                    "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral",
                    "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod",
                    "darkgray", "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen",
                    "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue",
                    "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue",
                    "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia",
                    "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "grey", "green", "greenyellow", "honeydew",
                    "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush",
                    "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow",
                    "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon", "lightseagreen",
                    "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow",
                    "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine",
                    "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue",
                    "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream",
                    "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab",
                    "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise",
                    "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum",
                    "powderblue", "purple", "red", "rosybrown", "royalblue", "saddlebrown",
                    "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue",
                    "slateblue", "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", "teal",
                    "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen"})
    private String color = "skyblue";
    @WidgetPluginProperty(name = "style", initValue = "filled", description = "The style of the shape", valueList = {"filled", "outlined"})
    private String style = "filled";
    @WidgetPluginProperty(name = "direction", initValue = "LR", description = "LR, RL, TB, BT", valueList = {"LR", "RL", "TB", "BT"})
    private String rankdir = "LR";
    @WidgetPluginProperty(name = "splines", initValue = "true", description = "Draw endges with splines", valueList = {"true", "false"})
    private String splines = "true";
    @WidgetPluginProperty(name = "arrow size", initValue = "0.5", description = "Arrow size")
    private String arrowsize = "0.5";
    @WidgetPluginProperty(name = "node separation", initValue = "0.1", description = "Minimum space between two adjacent nodes in the same rank, in inches.", valueList = {"0", "0.05", "0.1", "0.15", "0.2", "0.25", "0.3", "0.35", "0.4", "0.45", "0.5"})
    private String nodesep = "0.1";
    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size", valueList = {"true", "false"})
    private boolean resizeRegionEnabled = true;

    public WidgetGraphvizDotMindMap(ActiveRegionContext region) {
        super(region);
    }

    @Override
    protected String prepareDot(String text) {
        super.setCmdLineParams(this.cmdLineParams);
        List<String> path = new ArrayList<String>();
        String lines[] = text.split("\n");
        String dotCode = "digraph G {\n";
        dotCode += "graph [\n";
        dotCode += "	splines=" + splines + ",\n";
        dotCode += "	rankdir=\"" + rankdir + "\",\n";
        dotCode += "	nodesep=" + nodesep + "\n";
        dotCode += "]\n";
        dotCode += "node [\n";
        dotCode += "    shape = " + shape + ",\n";
        dotCode += "    height = auto,\n";
        dotCode += "    fontsize = " + fontsize + ",\n";
        dotCode += "    color = " + color + ",\n";
        dotCode += "    style = " + style + ",\n";
        dotCode += "    fontname = \"" + fontName + "\" ];\n";
        dotCode += "edge [\n";
        dotCode += "    arrowsize = " + arrowsize + ",\n";
        dotCode += "    fontsize = " + fontsize + ",\n";
        dotCode += "    fontname = \"" + fontName + "\" ];\n";
        int prevLevel = 0;
        String prevLine = "";
        boolean inLiteralMode = false;
        for (String line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }
            if (line.trim().equalsIgnoreCase("dot:")) {
                inLiteralMode = true;
                continue;
            } else if (inLiteralMode) {
                dotCode += line + "\n";
                continue;
            }
            if (line.contains("[")) {
                int n1 = line.indexOf("[");
                line = line.substring(0, n1);
            }
            int level = getLevel(line);
            if (level > prevLevel) {
                path.add(prevLine);
            } else if (level < prevLevel) {
                while (path.size() > 0) {
                    String parent = path.remove(path.size() - 1);
                    if (level >= getLevel(parent)) {
                        break;
                    }
                }
            }
            if (path.size() > 0) {
                String parent = path.get(path.size() - 1);
                if (!parent.isEmpty()) {
                    dotCode += "    \"" + parent.trim() + "\" -> \"" + line.trim() + "\";\n";
                } else {
                    dotCode += "    \"" + line.trim() + "\";\n";
                }
            } else {
                dotCode += "    \"" + line.trim() + "\";\n";
            }
            prevLine = line;
            prevLevel = level;
        }

        Map<Integer, String> settings = new HashMap<Integer, String>();
        for (String line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                break;
            }
            int level = getLevel(line);

            Map<Integer, String> oldSettings = settings;
            settings = new HashMap<Integer, String>();
            for (int i : oldSettings.keySet()) {
                if (i < level) {
                    settings.put(i, oldSettings.get(i));
                }
            }
            int n1 = line.indexOf("[");
            if (n1 > 0) {
                int n2 = line.indexOf("]");
                if (n1 >= 0 && n2 > n1) {
                    settings.put(level, line.substring(n1 + 1, n2));
                    dotCode += "    \"" + line.substring(0, n1).trim() + "\" [" + settings.get(level) + "];\n";
                }
            } else {
                if (settings.size() > 0) {
                    String strSetting = null;
                    for (int i = level; i >= 0; i--) {
                        strSetting = settings.get(i);
                        if (strSetting != null) {
                            break;
                        }
                    }
                    if (strSetting != null) {
                        dotCode += "    \"" + line.trim() + "\" [" + strSetting + "];\n";
                    } else {
                        dotCode += "    \"" + line.trim() + "\";\n";
                    }
                }
            }
        }

        dotCode += "}\n";

        super.setResizeRegionEnabled(this.resizeRegionEnabled);
        return dotCode;
    }

    private static int getLevel(String line) {
        int level = 0;
        line = line.replace("\t", "    ");
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                level++;
            } else {
                break;
            }
        }

        return level;
    }
}
