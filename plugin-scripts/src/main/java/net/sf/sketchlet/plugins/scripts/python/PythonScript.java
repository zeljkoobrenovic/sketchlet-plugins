/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.scripts.python;

import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

/**
 * @author cuypers
 */
@PluginInfo(name = "Python", type = "script", properties = {"extension=py"})
public class PythonScript extends ScriptPluginProxy {

    private ScriptEngineManager manager;

    public PythonScript(File scriptFile) {
        super(scriptFile);
        manager = new ScriptEngineManager();
        setEngine(manager.getEngineByName("python"));
    }

    @Override
    public void loadScript(FileReader scriptFile) throws Exception {
        setContext(getEngine());
        getEngine().eval(scriptFile);
    }
}
