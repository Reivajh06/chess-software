package reivajh06.plugins;

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
