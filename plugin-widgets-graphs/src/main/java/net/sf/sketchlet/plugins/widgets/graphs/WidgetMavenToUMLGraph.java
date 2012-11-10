/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;
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
 *
 * @author zobrenovic
 */
@PluginInfo(name = "UMLGraph / Maven POM", type = "widget", group="UML", position = 286)
@WidgetPluginTextItems(initValue = "")
@WidgetPluginLinks(links = {
    "Maven POM Reference; http://maven.apache.org/pom.html"
})
public class WidgetMavenToUMLGraph extends WidgetUMLGraph {
    //

    @WidgetPluginProperty(name = "dot parameters", initValue = "",
    description = "Additional parameters to be sent to the dot program",
    valueList = {"-Gratio=0.7 -Eminlen=2"})
    protected String cmdLineParams = "";
    //
    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size")
    protected boolean resizeRegion = true;

    public WidgetMavenToUMLGraph(ActiveRegionContext region) {
        super(region);
    }
    String code = "";
    String prevText = "";
    @WidgetPluginProperty(name = "show dependencies", initValue = "true", description = "Show/hide dependencvies")
    boolean showDependencies = true;
    @WidgetPluginProperty(name = "show exclusions", initValue = "true", description = "Show/hide dependency exclusions")
    boolean showExclusions = true;
    @WidgetPluginProperty(name = "show plugins", initValue = "true", description = "Show/hide plugins")
    boolean showPlugins = true;
    @WidgetPluginProperty(name = "show artifact items", initValue = "true", description = "Show/hide plugins")
    boolean showArtifactItems = true;

    @Override
    protected String getUMLGraphCode() {
        String text = getActiveRegionContext().getWidgetItemText();
        code = MavenPOMConfigSaxLoader.parse(this, text);

        super.resizeRegion = this.resizeRegion;
        super.cmdLineParams = this.cmdLineParams;
        return code;
    }
}

class MavenPOMConfigSaxLoader extends DefaultHandler {

    public MavenPOMConfigSaxLoader(WidgetMavenToUMLGraph widget) {
        super();
        this.widget = widget;
    }
    private Map<String, Elem> elems = new HashMap<String, Elem>();
    private String[] currentAssociation;
    private Elem currentProject;
    private Elem currentDependency;
    private Elem currentPlugin;
    private Elem currentExecution;
    private Elem currentArtifactItem;
    private Elem currentGroup;
    private Elem projectGroup;
    private final static Map<String, String> stereotypeColors = new HashMap<String, String>();
    private String currentElement;
    private StringBuffer result = new StringBuffer();
    private List<String> path = new ArrayList<String>();

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

    private static String getColor(String stereotype) {
        String color = stereotypeColors.get(stereotype);
        return color != null ? color : stereotypeColors.get("");
    }
    WidgetMavenToUMLGraph widget;

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
        for (Elem b : elems.values()) {
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
    private boolean inDependencies = false;
    private boolean inPlugins = false;

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        if (strElem.equals("dependency")) {
            inDependencies = true;
            inPlugins = false;
        } else if (strElem.equals("plugin")) {
            inDependencies = false;
            inPlugins = true;
        }

        path.add(strElem);

        currentElement = strElem;
        strCharacters = "";
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
        if (strElem.equals("dependency")) {
            inDependencies = false;
        }
        if (strElem.equals("plugin")) {
            inDependencies = false;
        }
    }
    private String strCharacters = "";

    @Override
    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            strCharacters += strValue;
        }
    }

    public void processCharacters() {
        strCharacters = strCharacters.replace("\\n", "\n");
        strCharacters = strCharacters.replace("\\r", "\r");
        strCharacters = strCharacters.replace("\\t", "\t");
        strCharacters = strCharacters.replace("&lt;", "<");
        strCharacters = strCharacters.replace("&gt;", ">");
        strCharacters = strCharacters.replace("&amp;", "&");
        if (currentElement == null) {
            return;
        } else if (path().equals("/project/packaging/")) {
            if (this.currentProject != null) {
                this.currentProject.extraParams.add("@tagvalue packaging " + strCharacters);
            }
        } else if (path().equals("/project/version/")) {
            if (this.currentProject != null) {
                this.currentProject.extraParams.add("@tagvalue version " + strCharacters);
            }
        } else if (path().equals("/project/groupId/")) {
            String id = id("group_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "group";
                elems.put(id, e);
            }
            this.currentGroup = e;
        } else if (path().equals("/project/artifactId/")) {
            String id = id("project_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "project";
                elems.put(id, e);
            }

            this.currentProject = e;
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
            }
        } else if (widget.showPlugins && path().equals("/project/build/plugins/plugin/groupId/")) {
            String id = id("group_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "group";
                elems.put(id, e);
            }
            this.currentGroup = e;
        } else if (widget.showPlugins && path().equals("/project/build/plugins/plugin/artifactId/")) {
            String id = id("plugin_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "plugin";
                elems.put(id, e);
            }
            currentAssociation = new String[]{"- imports -", e.id};
            currentProject.associations.add(currentAssociation);
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
                this.currentGroup = null;
            }
            this.currentPlugin = e;
        } else if (widget.showPlugins && path().equals("/project/build/plugins/plugin/executions/execution/id/")) {
            String id = id("execution_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "execution";
                elems.put(id, e);
                e.extraParams.add("@opt operations");
            }
            if (this.currentPlugin != null) {
                this.currentPlugin.extraParams.add("@has - - - " + e.id);
            }
            this.currentExecution = e;
        } else if (widget.showPlugins && path().equals("/project/build/plugins/plugin/executions/execution/goals/goal/")) {
            String id = id("goal_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "goal";
                elems.put(id, e);
            }
            if (this.currentExecution != null) {
                this.currentExecution.extraParams.add("@has - - - " + e.id);
            }
        } else if (widget.showPlugins && widget.showArtifactItems && path().equals("/project/build/plugins/plugin/executions/execution/configuration/artifactItems/artifactItem/artifactId/")) {
            String id = id("artifactItem_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "artifact-item";
                elems.put(id, e);
            }
            if (this.currentExecution != null) {
                this.currentExecution.extraParams.add("@navassoc - - - " + e.id);
            }
            this.currentArtifactItem = e;
        } else if (widget.showPlugins && widget.showArtifactItems && path().equals("/project/build/plugins/plugin/executions/execution/configuration/artifactItems/artifactItem/outputDirectory/")) {
            this.currentArtifactItem.extraParams.add("@note output directory:\n   " + strCharacters.trim());
        } else if (widget.showDependencies && path().equals("/project/dependencies/dependency/groupId/")) {
            String id = id("group_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "group";
                elems.put(id, e);
            }
            this.currentGroup = e;
        } else if (widget.showDependencies && path().equals("/project/dependencies/dependency/artifactId/")) {
            String id = id("artifact_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "artifact";
                elems.put(id, e);
            }
            currentAssociation = new String[]{"- depends -", e.id};
            currentProject.associations.add(currentAssociation);
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
                this.currentGroup = null;
            }
            this.currentDependency = e;
        } else if (widget.showDependencies && widget.showExclusions && path().equals("/project/dependencies/dependency/exclusions/exclusion/groupId/")) {
            String id = id("group_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "group";
                elems.put(id, e);
            }
            this.currentGroup = e;
        } else if (widget.showDependencies && widget.showExclusions && path().equals("/project/dependencies/dependency/exclusions/exclusion/artifactId/")) {
            String id = id("artifact_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "artifact";
                elems.put(id, e);
            }
            currentAssociation = new String[]{"- exclusion -", e.id};
            currentDependency.associations.add(currentAssociation);
            if (this.currentGroup != null) {
                this.currentGroup.extraParams.add("@has - - - " + e.id);
                this.currentGroup = null;
            }
        } else if (path().equals("/project/repositories/repository/id/")) {
            String id = id("repository_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "repository";
                elems.put(id, e);
            }
        } else if (path().equals("/project/repositories/repository/id/")) {
            String id = id("repository_", strCharacters);
            Elem e = elems.get(id);
            if (e == null) {
                e = new Elem(id, strCharacters);
                e.stereotype = "repository";
                elems.put(id, e);
            }
        } else if (currentElement.equals("scope")) {
            if (currentAssociation != null) {
                currentAssociation[0] = "- \"depends[" + strCharacters + "]\" -";
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
    private static final String symbols = "$_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

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
}