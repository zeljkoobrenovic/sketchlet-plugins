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
/*@PluginInfo(name = "UMLGraph / Spring Configuration", type = "widget", group = "UML", position = 285)
@WidgetPluginTextItems(initValue = "")
@WidgetPluginLinks(links = {
        "Spring Reference; http://static.springsource.org/spring/docs/2.5.x/reference/xsd-config.html"
})*/
public class WidgetSpringToUMLGraph extends WidgetUMLGraph {

    @WidgetPluginProperty(name = "dot parameters", initValue = "", description = "Additional parameters to be sent to the dot program", valueList = {"-Gratio=0.7 -Eminlen=2"})
    private String cmdLineParams = "";

    @WidgetPluginProperty(name = "resize region", initValue = "true", description = "Resize the region to fit the generated image size")
    private boolean resizeRegionEnabled = true;

    private String umlGraphCode = "";

    public WidgetSpringToUMLGraph(ActiveRegionContext region) {
        super(region);
    }

    @Override
    protected String getUMLGraphCode() {
        String text = getActiveRegionContext().getWidgetItemText();
        umlGraphCode = SpringConfigSaxLoader.parse(text);

        super.setCmdLineParams(this.cmdLineParams);
        super.setResizingRegion(this.resizeRegionEnabled);
        return umlGraphCode;
    }
}

class SpringConfigSaxLoader extends DefaultHandler {
    private Map<String, Bean> beans = new HashMap<String, Bean>();
    private Bean globalStateBean = new Bean("");
    private Bean currentBean;
    private static Map<String, String> stereotypeColors = new HashMap<String, String>();
    private String currentElement;
    private StringBuffer result = new StringBuffer();
    private String characters = "";

    static {
        stereotypeColors.put("", "skyblue");
        stereotypeColors.put("bean", "skyblue");
        stereotypeColors.put("action-state", "lightblue");
        stereotypeColors.put("decision-state", "darkseagreen");
        stereotypeColors.put("view-state", "yellow");
        stereotypeColors.put("end-state", "gray");
    }

    public SpringConfigSaxLoader() {
        super();
    }

    private static String getColor(String stereotype) {
        String color = stereotypeColors.get(stereotype);
        return color != null ? color : stereotypeColors.get("");
    }

    public static String parse(String strCode) {
        SpringConfigSaxLoader handler = new SpringConfigSaxLoader();
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
    }

    @Override
    public void endDocument() {
        Map<String, String> classes = new HashMap<String, String>();
        for (Bean b : beans.values()) {
            result.append("/**\n");
            result.append(" * @stereotype " + b.stereotype + "\n");
            result.append(" * @opt nodefillcolor " + getColor(b.stereotype) + "\n");
            for (String link[] : b.associations) {
                result.append(" * @navassoc " + link[0] + " " + link[1] + "\n");
            }
            if (b.stereotype.endsWith("-state") && !b.stereotype.equalsIgnoreCase("end-state")) {
                for (String link[] : globalStateBean.associations) {
                    result.append(" * @navassoc " + link[0] + " " + link[1] + "\n");
                }
            }
            for (String param : b.extraParams) {
                result.append(" * " + param + "\n");
            }
            result.append(" */");
            result.append("class " + b.name);
            if (!b.className.isEmpty()) {
                String shortName = getShortClassName(b.className, false);
                classes.put(shortName, shortName);
                result.append(" extends " + shortName);
            }
            result.append(" {}" + "\n");
        }
        for (String strClass : classes.values()) {
            result.append("class " + strClass + "{}" + "\n");
        }
    }

    private static String getShortClassName(String className, boolean asField) {
        if (className != null) {
            int n = className.lastIndexOf(".");
            if (n > 0) {
                if (asField) {
                    className = className.substring(n + 1, n + 2).toLowerCase() + className.substring(n + 2);
                } else {
                    className = className.substring(n + 1);
                }
            }
        }
        return className;
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        currentElement = strElem;
        characters = "";
        String id = atts.getValue("id");
        if (id == null && strElem.equalsIgnoreCase("pipe")) {
            id = atts.getValue("name");
        }

        if (id == null && strElem.equalsIgnoreCase("bean")) {
            id = getShortClassName(atts.getValue("class"), true);
        } else if (id == null && strElem.equalsIgnoreCase("global-transitions")) {
            currentBean = globalStateBean;
        }

        if (id != null && !id.isEmpty()) {
            Bean b = beans.get(id);
            if (b == null) {
                b = new Bean(id);
                beans.put(id, b);
            }
            b.stereotype = strElem;
            if (!strElem.equalsIgnoreCase("pointcut")) {
                currentBean = b;
            } else {
                String exp = atts.getValue("expression");
                if (exp != null && !exp.isEmpty()) {
                    b.extraParams.add("@note " + exp);
                }
            }
            String className = atts.getValue("class");
            if (className != null) {
                b.className = className;
            }
        }
        if (strElem.equalsIgnoreCase("if")) {
            currentBean.extraParams.add("@note Test: " + atts.getValue("test"));
        }

        for (int i = 0; i < atts.getLength(); i++) {
            String attName = atts.getQName(i);
            String value = atts.getValue(i);
            if (attName != null
                    && (attName.equals("ref") || attName.equals("then") || attName.equals("else")
                    || attName.equals("to") || attName.equals("bean") || attName.endsWith("-ref") || attName.endsWith("referenceId"))) {
                if (strElem.equalsIgnoreCase("aspect")) {
                    currentBean = new Bean("aspect_" + value);
                    currentBean.stereotype = "aspect";
                    beans.put(currentBean.name, currentBean);
                }
                Bean b = beans.get(value);
                if (b == null) {
                    b = new Bean(value);
                    beans.put(value, b);
                }
                if (currentBean != null) {
                    if (b.stereotype.equalsIgnoreCase("pointcut")) {
                        String meth = atts.getValue("method");
                        if (meth != null && !meth.isEmpty()) {
                            currentBean.associations.add(new String[]{
                                    "- \"" + strElem + " / " + meth + "\" -", b.name});
                        }
                    } else {
                        String assocName = "-";
                        if (strElem.equalsIgnoreCase("property")) {
                            String nameAtt = atts.getValue("name");
                            if (nameAtt != null && !nameAtt.isEmpty()) {
                                assocName = "\"" + nameAtt + "\"";
                            }
                        } else if (attName.equalsIgnoreCase("to")) {
                            String nameAtt = atts.getValue("on");
                            if (nameAtt != null && !nameAtt.isEmpty()) {
                                assocName = "\"" + nameAtt + "\"";
                            } else {
                                nameAtt = getShortClassName(atts.getValue("on-exception"), false);
                                if (nameAtt != null && !nameAtt.isEmpty()) {
                                    assocName = "\"" + nameAtt + "\"";
                                }
                            }
                        } else if (attName.equalsIgnoreCase("then")) {
                            assocName = "\"if true\"";
                        } else if (attName.equalsIgnoreCase("else")) {
                            assocName = "\"if false\"";
                        } else {
                            assocName = strElem;
                        }
                        currentBean.associations.add(new String[]{"- " + assocName + " -", b.name});
                    }
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        this.processCharacters();

        currentElement = null;
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
        }
        if (currentElement.equalsIgnoreCase("slide-title")) {
        }
    }

    class Bean {

        public Bean(String name) {
            this.name = name;
        }

        String stereotype = "";
        String name = "";
        String className = "";
        List<String[]> associations = new ArrayList<String[]>();
        List<String> extraParams = new ArrayList<String>();
    }
}