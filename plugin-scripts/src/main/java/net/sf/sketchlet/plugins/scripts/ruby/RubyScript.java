/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.scripts.ruby;

import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

/**
 * @author cuypers
 */
@PluginInfo(name = "Ruby", type = "script", properties = {"extension=rb"})
public class RubyScript extends ScriptPluginProxy {

    private ScriptEngineManager mgr;

    public RubyScript(File scriptFile) {
        super(scriptFile);
        mgr = new ScriptEngineManager();
        setEngine(mgr.getEngineByName("ruby"));
    }

    public void loadScript(FileReader scriptFile) throws Exception {
        setContext(getEngine());
        getEngine().eval(scriptFile);
    }
}
