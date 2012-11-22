/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.scripts.javascript;

import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author cuypers
 */
@PluginInfo(name = "Javascript", type = "script", properties = {"extension=js"})
public class JSScript extends ScriptPluginProxy {

    private ScriptEngineManager mgr;
    private Invocable invocableEngine;

    public JSScript(File scriptFile) {
        super(scriptFile);
        mgr = new ScriptEngineManager();
        setEngine(mgr.getEngineByName("JavaScript"));
    }

    @Override
    public void loadScript(FileReader scriptFile) throws Exception {
        setContext(getEngine());

        String code = "";
        BufferedReader reader = new BufferedReader(scriptFile);
        String line;
        while ((line = reader.readLine()) != null) {
            code += line + "\n";
        }
        code = VariablesBlackboardContext.getInstance().populateTemplate(code);
        getEngine().eval(code);
        this.invocableEngine = (Invocable) getEngine();
    }

    @Override
    public void callScript(Object sender, String triggerVariable, String value, String oldValue) throws Exception {
        this.invocableEngine.invokeFunction("variableUpdated", triggerVariable, value);
    }
}
