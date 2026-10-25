package reivajh06.plugins;

import java.io.BufferedReader;

public class PluginReader {

	private final BufferedReader reader;

	public PluginReader(BufferedReader reader) {
		this.reader = reader;
	}

	public String read(String prefix) throws PluginException {
		String line;

		try {
			while((line = reader.readLine()) != null) {
				if(line.startsWith(prefix)) return line;
			}
		} catch (Exception e) {
			throw new PluginException("Error while reading plugin response", e);
		}

		return null;
	}
}
