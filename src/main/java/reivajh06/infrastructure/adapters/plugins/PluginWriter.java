package reivajh06.infrastructure.adapters.plugins;

import reivajh06.infrastructure.adapters.plugins.PluginException;

import java.io.BufferedWriter;
import java.io.IOException;

public class PluginWriter {

	private final BufferedWriter writer;

	public PluginWriter(BufferedWriter writer) {
		this.writer = writer;
	}

	public void send(String msg) throws PluginException {
		try {
			writer.write(msg);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new PluginException("Error while writing to plugin", e);
		}
	}
}
