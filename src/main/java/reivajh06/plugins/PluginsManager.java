package reivajh06.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PluginsManager {

    public File pluginsFolder;
    public Map<String, PluginExecutor> plugins;

    public PluginsManager(File home) {
        plugins = new HashMap<>();
        pluginsFolder = new File(home, "plugins");
        pluginsFolder.mkdir();
    }

    public void reloadPlugins() {
        File[] content = pluginsFolder.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));

        plugins = Arrays.stream(content).collect(Collectors.toMap(File::getName, f -> pluginExecutor(f.getName())));
    }

    public PluginExecutor pluginExecutor(String pluginName) {
        return new PluginExecutor(pluginName, pluginsFolder);
    }

    public List<String> pluginNames() {
        return plugins.keySet().stream().toList();
    }
}
