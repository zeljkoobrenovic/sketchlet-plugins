/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.scripts.beanshell;

import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

/**
 * @author cuypers
 */
@PluginInfo(name = "Bean Shell", type = "script", properties = {"extension=bsh"})
public class BeanShellScript extends ScriptPluginProxy {

    private ScriptEngineManager mgr;

    public BeanShellScript(File scriptFile) {
        super(scriptFile);
        mgr = new ScriptEngineManager();
        setEngine(mgr.getEngineByName("bsh"));
    }

    @Override
    public void loadScript(FileReader scriptFile) throws Exception {
        setContext(getEngine());
        getEngine().eval(scriptFile);
    }
}
