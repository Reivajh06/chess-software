package reivajh06.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PluginsManager {

    public String pluginsFolder = "C:/Program Files (x86)/Common Files/archchess/plugins";
    public Map<String, PluginExecutor> plugins;

    public PluginsManager() {
        plugins = new HashMap<>();
    }

    public boolean setPluginsFolder(String pluginsFolder) {
        File file = new File(pluginsFolder);

        if(!file.isDirectory() || !file.exists()) return false;
        if(!file.canRead() || !file.canWrite()) return false;

        this.pluginsFolder = pluginsFolder;
        return true;
    }

    public void reloadPlugins() {
        File folder = new File(pluginsFolder);

        File[] content = folder.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));

        plugins = Arrays.stream(content).collect(Collectors.toMap(File::getName, f -> pluginExecutor(f.getName())));
    }

    public PluginExecutor pluginExecutor(String pluginName) {
        return new PluginExecutor(pluginName, pluginsFolder);
    }
}
