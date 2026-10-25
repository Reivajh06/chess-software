package reivajh06.plugins;

import java.io.IOException;

public class PluginExecutor {

	static final String PREFIX = "$archchess:";

	private final String pluginName;
	private final String pluginsRoot;
	private Process process;

	public PluginExecutor(String pluginName, String pluginsRoot) {
		this.pluginName = pluginName;
		this.pluginsRoot = pluginsRoot;
	}

	public void start() throws IOException {
		process = new ProcessBuilder()
				.command("java", "-jar", pluginsRoot + "/" + pluginName, "")
				.start();
	}

	public String name() throws IOException {
		try {
			PluginWriter writer = new PluginWriter(process.outputWriter());
			writer.send("name");

			PluginReader reader = new PluginReader(process.inputReader());
			return reader.read(PREFIX);

		} catch(Exception e) {
			return null;
		}
	}

	public Object requestMove(String fen) {
		try {
			PluginWriter writer = new PluginWriter(process.outputWriter());
			writer.send("move %s".formatted(fen));

			PluginReader reader = new PluginReader(process.inputReader());

			return new PluginResponse(reader.read(PREFIX));

		} catch(PluginException e) {
			return null;
		}
	}

	public void exit() {
		try {
			PluginWriter writer = new PluginWriter(process.outputWriter());
			writer.send("exit");

		} catch(PluginException _) {
		}
	}
}