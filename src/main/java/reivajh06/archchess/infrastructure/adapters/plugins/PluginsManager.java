package reivajh06.archchess.infrastructure.adapters.plugins;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PluginsManager {

    public File pluginsFolder;
    public Map<String, PluginExecutor> plugins;

    public PluginsManager(String pluginsPath) {
        plugins = new HashMap<>();
        this.pluginsFolder = new File(pluginsPath);
        this.pluginsFolder.mkdir();
    }

    public void reloadPlugins() {
        File[] content = pluginsFolder.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));

        plugins = Arrays.stream(content).collect(Collectors.toMap(File::getName, f -> pluginExecutor(f.getName())));
    }

    public void configurePluginsReload() {
        Thread thread = new Thread(() -> {
            while(true) {
                reloadPlugins();
                try {
                    Thread.sleep(Duration.ofMinutes(3));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public PluginExecutor pluginExecutor(String pluginName) {
        return new PluginExecutor(pluginName, pluginsFolder);
    }

    public List<String> pluginNames() {
        return plugins.keySet().stream().toList();
    }
}
