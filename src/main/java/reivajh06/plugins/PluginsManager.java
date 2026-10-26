package reivajh06.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginsManager {

    public String pluginsFolder = "C:\\Program Files (x86)\\Common Files\\archchess\\plugins\\";
    public List<String> plugins;

    public PluginsManager(List<String> plugins) {
        this.plugins = plugins;
    }

    public boolean setPluginsFolder(String pluginsFolder) {
        File file = new File(pluginsFolder);

        if(!file.isDirectory() || !file.exists()) return false;
        if(!file.canRead() || !file.canRead()) return false;

        this.pluginsFolder = pluginsFolder;
        return true;
    }

    public void inspectFolder() {
        File folder = new File(pluginsFolder);

        File[] content = folder.listFiles(f -> f.isFile() && f.toPath().endsWith(".jar"));

        plugins = new ArrayList<>(
                Arrays.stream(content)
                .map(File::getName)
                .toList()
        );
    }

    public PluginExecutor pluginExecutor(String pluginName) {
        return new PluginExecutor(pluginName, pluginsFolder);
    }
}
