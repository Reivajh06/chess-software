package reivajh06.archchess.infrastructure.adapters.plugins;

import reivajh06.plugins.Plugin;
import reivajh06.plugins.PluginException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class PluginsManager {

	public File pluginsFolder;
    public Map<String, Plugin> plugins;
	private URLClassLoader classLoader;

    public PluginsManager(String pluginsPath) {
        plugins = new HashMap<>();
        this.pluginsFolder = new File(pluginsPath);
        this.pluginsFolder.mkdir();
    }

    public void reloadPlugins() throws PluginException {
        URL[] urls = Arrays.stream(pluginsFolder.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"))).map(f -> {
	        try {
				return f.toURI().toURL();
	        } catch (MalformedURLException e) {
				throw new RuntimeException("Error while loading plugins urls", e);
			}
        }).toArray(URL[]::new);

		classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
		ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, classLoader);

		plugins.putAll(loader.stream()
				.filter(p -> plugins.get(p.get().name()) == null)
				.collect(Collectors.toMap(p -> p.get().name(), ServiceLoader.Provider<Plugin>::get)));
		plugins.values().forEach(Plugin::start);
	}

    public void configurePluginsReload() {
        Thread thread = new Thread(() -> {
            while(true) {
				try {
					reloadPlugins();
					System.out.println("Plugins loaded: %s".formatted(pluginNames()));
					Thread.sleep(Duration.ofMinutes(1));

				} catch (PluginException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
        });

		thread.setName("plugins-reloader");
		thread.setDaemon(true);
        thread.start();
    }

	public List<String> pluginNames() {
        return plugins.keySet().stream().toList();
    }

    public Plugin plugin(String name) {
        return plugins.get(name);
    }
}
