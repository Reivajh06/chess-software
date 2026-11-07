package reivajh06.archchess.infrastructure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

	private static final String DEFAULT_HOME_PATH = "temp";
	private static final String DEFAULT_DB_PATH = "temp/archchess.db";
	private static final String DEFAULT_DL_PATH = "temp/datalake";
	private static final int DEFAULT_API_PORT = 5000;
	private static final String DEFAULT_PLUGINS_PATH = "temp/plugins";

	private static String configPath;

	private final String homePath;
	private final String datalakePath;
	private final String databasePath;
	private final int apiPort;
	private final String pluginsPath;

	public static void configPath(String configPath) {
		Configuration.configPath = configPath;
	}

	public static Configuration loadConfigFile() {
		Properties properties = new Properties();

		if(configPath == null) {
			try (InputStream in = Configuration.class.getResourceAsStream("/archchess.properties")) {
				if (in != null) properties.load(in);

			} catch (IOException e) {
				throw new RuntimeException("Error while reading the configuration file", e);
			}

		} else {
			File file = new File(configPath);

			if(!file.exists()) throw new RuntimeException("Configuration file %s not found!".formatted(configPath));
			if(!file.isFile() || !configPath.endsWith(".properties")) throw new RuntimeException("%s is not a configuration file!".formatted(configPath));

			try (InputStream in = new FileInputStream(file)) {
				properties.load(in);

			} catch (IOException e) {
				throw new RuntimeException("Error while reading the configuration file", e);
			}
		}

		return new Configuration(
				properties.getProperty("home.path", DEFAULT_HOME_PATH),
				properties.getProperty("datalake.path", DEFAULT_DL_PATH),
				properties.getProperty("database.path", DEFAULT_DB_PATH),
				Integer.parseInt(properties.getProperty("server.port", String.valueOf(DEFAULT_API_PORT))),
				properties.getProperty("plugins.path", DEFAULT_PLUGINS_PATH)
		);
	}

	private Configuration(String homePath, String datalakePath, String databasePath, int apiPort, String pluginsPath) {
		this.homePath = homePath;
		this.datalakePath = datalakePath;
		this.databasePath = databasePath;
		this.apiPort = apiPort;
		this.pluginsPath = pluginsPath;
	}

	public String homePath() {
		return homePath;
	}

	public String datalakePath() {
		return datalakePath;
	}

	public String databasePath() {
		return databasePath;
	}

	public int apiPort() {
		return apiPort;
	}

	public String pluginsPath() {
		return pluginsPath;
	}
}
