/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.scripts.beanshell;

import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

/**
 * @author cuypers
 */
@PluginInfo(name = "Bean Shell", type = "script", properties = {"extension=bsh"})
public class BeanShellScript extends ScriptPluginProxy {

    ScriptEngineManager mgr;
    Invocable invocableEngine;

    public BeanShellScript(File scriptFile) {
        super(scriptFile);
        mgr = new ScriptEngineManager();
        engine = mgr.getEngineByName("bsh");
    }

    public void loadScript(FileReader scriptFile) throws Exception {
        setContext(engine);
        engine.eval(scriptFile);
        // this.invocableEngine = (Invocable) bshEngine;
    }
}
