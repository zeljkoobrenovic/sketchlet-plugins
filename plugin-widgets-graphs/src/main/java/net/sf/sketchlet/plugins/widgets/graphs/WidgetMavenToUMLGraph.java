package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zobrenovic
 */
/*
@PluginInfo(name = "UMLGraph / Maven POM", type = "widget", group = "UML", position = 286)
@WidgetPluginTextItems(initValue = "")
@WidgetPluginLinks(links = {
        "Maven POM Reference; http://maven.apache.org/pom.html"
})
*/
public class WidgetMavenToUMLGraph extends WidgetUMLGraph {
    private String code = "";

    @WidgetPluginProperty(name = "dot parameters", initValue = "",
            description = "Additional parameters to be sent to the dot program",
            valueList = {"-Gratio=0.7 -Eminlen=2"})
    private String cmdLineParams = "";

    @WidgetPluginProperty(name = "show dependencies", initValue = "true", description = "Show/hide dependencvies")
    private boolean showDependenciesEnabled = true;

    @WidgetPluginProperty(name = "show exclusions", initValue = "true", description = "Show/hide dependency exclusions")
    private boolean showExclusionsEnabled = true;

    @WidgetPluginProperty(name = "show plugins", initValue = "true", description = "Show/hide plugins")
    private boolean showPluginsEnabled = true;

    @WidgetPluginProperty(name = "show artifact items", initValue = "true", description = "Show/hide plugins")
    private boolean showArtifactItemsEnabled = true;

    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size")
    private boolean resizingRegionEnabled = true;


    public WidgetMavenToUMLGraph(ActiveRegionContext region) {
        super(region);
    }

    @Override
    protected String getUMLGraphCode() {
        String text = getActiveRegionContext().getWidgetItemText();
        code = MavenPOMConfigSaxLoader.parse(this, text);

        super.setResizingRegion(this.isResizingRegionEnabled());
        super.setCmdLineParams(this.cmdLineParams);
        return code;
    }

    public boolean isShowPluginsEnabled() {
        return showPluginsEnabled;
    }

    public boolean isShowDependenciesEnabled() {
        return showDependenciesEnabled;
    }

    public boolean isShowExclusionsEnabled() {
        return showExclusionsEnabled;
    }

    public boolean isShowArtifactItemsEnabled() {
        return showArtifactItemsEnabled;
    }

    public boolean isResizingRegionEnabled() {
        return resizingRegionEnabled;
    }
}

class MavenPOMConfigSaxLoader extends DefaultHandler {

    private Map<String, Elem> elements = new HashMap<String, Elem>();
    private String[] currentAssociation;
    private Elem currentProject;
    private Elem currentDependency;
    private Elem currentPlugin;
    private Elem currentExecution;
    private Elem currentArtifactItem;
    private Elem currentGroup;
    private final static Map<String, String> stereotypeColors = new HashMap<String, String>();
    private String currentElement;
    private StringBuffer result = new StringBuffer();
    private List<String> path = new ArrayList<String>();
    private WidgetMavenToUMLGraph widget;
    private String characters = "";
    private static final String symbols = "$_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static {
        stereotypeColors.put("", "gainsboro");
        stereotypeColors.put("project", "ivory");
        stereotypeColors.put("group", "gray");
        stereotypeColors.put("artifact", "lightsteelblue");
        stereotypeColors.put("plugin", "darksalmon");
        stereotypeColors.put("execution", "coral");
        stereotypeColors.put("goal", "burlywood");
        stereotypeColors.put("artifact item", "bisque");
    }

    public MavenPOMConfigSaxLoader(WidgetMavenToUMLGraph widget) {
        super();
        this.widget = widget;
    }

    public static String parse(WidgetMavenToUMLGraph widget, String strCode) {
        MavenPOMConfigSaxLoader handler = new MavenPOMConfigSaxLoader(widget);
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            StringReader r = new StringReader(strCode);
            xr.parse(new InputSource(r));
            r.close();

            return handler.result.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void startDocument() {
        path.clear();
    }

    @Override
    public void endDocument() {
        Map<String, String> classes = new HashMap<String, String>();
        result.append("/**\n");
        result.append(" * @hidden\n");
        result.append(" * @opt nodefonttagsize 6\n");
        result.append(" * @opt nodefonttagname ariali\n");
        result.append(" */\n");
        result.append("class UMLOptions{}\n");
        for (Elem b : elements.values()) {
            result.append("/**\n");
            result.append(" * " + b.name + "\n");
            result.append(" * @opt commentname\n");
            result.append(" * @stereotype " + b.stereotype + "\n");
            result.append(" * @opt nodefillcolor " + getColor(b.stereotype) + "\n");
            for (String link[] : b.associations) {
                result.append(" * @navassoc " + link[0] + " " + link[1] + "\n");
            }
            for (String param : b.extraParams) {
                result.append(" * " + param + "\n");
            }
            result.append(" */\n");
            result.append("class " + b.id);
            result.append(" {}" + "\n");
        }
        for (String strClass : classes.values()) {
            result.append("class " + strClass + "{}" + "\n");
        }
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        path.add(strElem);

        currentElement = strElem;
        characters = "";
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        this.processCharacters();

        currentElement = null;
        if (path.size() > 0) {
            path.remove(path.size() - 1);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            characters += strValue;
        }
    }

    private void processCharacters() {
        characters = characters.replace("\\n", "\n");
        characters = characters.replace("\\r", "\r");
        characters = characters.replace("\\t", "\t");
        characters = characters.replace("&lt;", "<");
        characters = characters.replace("&gt;", ">");
        characters = characters.replace("&amp;", "&");
        if (currentElement == null) {
            return;
        } else if (path().equals("/project/packaging/")) {
            if (this.currentProject != null) {
                this.currentProject.extraParams.add("@tagvalue packaging " + characters);
            }
        } else if (path().equals("/project/version/")) {
            if (this.currentProject != null) {
                this.currentProject.extraParams.add("@tagvalue version " + characters);
            }
        } else if (path().equals("/project/groupId/")) {
            String id = id("group_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "group";
                elements.put(id, e);
            }
            this.currentGroup = e;
        } else if (path().equals("/project/artifactId/")) {
            String id = id("project_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "project";
                elements.put(id, e);
            }

            this.currentProject = e;
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
            }
        } else if (widget.isShowPluginsEnabled() && path().equals("/project/build/plugins/plugin/groupId/")) {
            String id = id("group_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "group";
                elements.put(id, e);
            }
            this.currentGroup = e;
        } else if (widget.isShowPluginsEnabled() && path().equals("/project/build/plugins/plugin/artifactId/")) {
            String id = id("plugin_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "plugin";
                elements.put(id, e);
            }
            currentAssociation = new String[]{"- imports -", e.id};
            currentProject.associations.add(currentAssociation);
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
                this.currentGroup = null;
            }
            this.currentPlugin = e;
        } else if (widget.isShowPluginsEnabled() && path().equals("/project/build/plugins/plugin/executions/execution/id/")) {
            String id = id("execution_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "execution";
                elements.put(id, e);
                e.extraParams.add("@opt operations");
            }
            if (this.currentPlugin != null) {
                this.currentPlugin.extraParams.add("@has - - - " + e.id);
            }
            this.currentExecution = e;
        } else if (widget.isShowPluginsEnabled() && path().equals("/project/build/plugins/plugin/executions/execution/goals/goal/")) {
            String id = id("goal_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "goal";
                elements.put(id, e);
            }
            if (this.currentExecution != null) {
                this.currentExecution.extraParams.add("@has - - - " + e.id);
            }
        } else if (widget.isShowPluginsEnabled() && widget.isShowArtifactItemsEnabled() && path().equals("/project/build/plugins/plugin/executions/execution/configuration/artifactItems/artifactItem/artifactId/")) {
            String id = id("artifactItem_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "artifact-item";
                elements.put(id, e);
            }
            if (this.currentExecution != null) {
                this.currentExecution.extraParams.add("@navassoc - - - " + e.id);
            }
            this.currentArtifactItem = e;
        } else if (widget.isShowPluginsEnabled() && widget.isShowArtifactItemsEnabled() && path().equals("/project/build/plugins/plugin/executions/execution/configuration/artifactItems/artifactItem/outputDirectory/")) {
            this.currentArtifactItem.extraParams.add("@note output directory:\n   " + characters.trim());
        } else if (widget.isShowDependenciesEnabled() && path().equals("/project/dependencies/dependency/groupId/")) {
            String id = id("group_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "group";
                elements.put(id, e);
            }
            this.currentGroup = e;
        } else if (widget.isShowDependenciesEnabled() && path().equals("/project/dependencies/dependency/artifactId/")) {
            String id = id("artifact_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "artifact";
                elements.put(id, e);
            }
            currentAssociation = new String[]{"- depends -", e.id};
            currentProject.associations.add(currentAssociation);
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
                this.currentGroup = null;
            }
            this.currentDependency = e;
        } else if (widget.isShowDependenciesEnabled() && widget.isShowExclusionsEnabled() && path().equals("/project/dependencies/dependency/exclusions/exclusion/groupId/")) {
            String id = id("group_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "group";
                elements.put(id, e);
            }
            this.currentGroup = e;
        } else if (widget.isShowDependenciesEnabled() && widget.isShowExclusionsEnabled() && path().equals("/project/dependencies/dependency/exclusions/exclusion/artifactId/")) {
            String id = id("artifact_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "artifact";
                elements.put(id, e);
            }
            currentAssociation = new String[]{"- exclusion -", e.id};
            currentDependency.associations.add(currentAssociation);
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
                this.currentGroup = null;
            }
        } else if (path().equals("/project/repositories/repository/id/")) {
            String id = id("repository_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "repository";
                elements.put(id, e);
            }
        } else if (path().equals("/project/repositories/repository/id/")) {
            String id = id("repository_", characters);
            Elem e = elements.get(id);
            if (e == null) {
                e = new Elem(id, characters);
                e.stereotype = "repository";
                elements.put(id, e);
            }
        } else if (currentElement.equals("scope")) {
            if (currentAssociation != null) {
                currentAssociation[0] = "- \"depends[" + characters + "]\" -";
            }
        }
    }

    private String path() {
        String strPath = "/";
        for (String p : path) {
            strPath += p + "/";
        }

        return strPath;
    }

    class Elem {

        public Elem() {
        }

        public Elem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        String stereotype = "";
        String name = "";
        String id;
        List<String[]> associations = new ArrayList<String[]>();
        List<String> extraParams = new ArrayList<String>();
    }

    private String id(String prefix, String name) {
        name = name.replace("${", "variable_");

        String temp = name;
        for (int i = 0; i < temp.length(); i++) {
            String c = temp.charAt(i) + "";
            if (!symbols.contains(c)) {
                name = name.replace(c, "_");
            }
        }

        return "_" + prefix + name;
    }

    private static String getColor(String stereotype) {
        String color = stereotypeColors.get(stereotype);
        return color != null ? color : stereotypeColors.get("");
    }
}