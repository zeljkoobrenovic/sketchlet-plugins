/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.scripts.python;

import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

/**
 * @author cuypers
 */
@PluginInfo(name = "Python", type = "script", properties = {"extension=py"})
public class PythonScript extends ScriptPluginProxy {

    Invocable invocableEngine;
    ScriptEngineManager mgr;

    public PythonScript(File scriptFile) {
        super(scriptFile);
        mgr = new ScriptEngineManager();
        engine = mgr.getEngineByName("python");
    }

    public void loadScript(FileReader scriptFile) throws Exception {
        setContext(engine);
        engine.eval(scriptFile);
    }
}
