package reivajh06;

import reivajh06.api.ChessAPI;
import reivajh06.database.Database;
import reivajh06.datalake.EventPublisher;
import reivajh06.datalake.EventReader;
import reivajh06.datalake.events.Event;
import reivajh06.plugins.PluginsManager;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class Archchess {

	private static final Archchess INSTANCE = new Archchess();
	private static final String DEFAULT_DB_PATH = "temp/archchess.db";
	private static final String DEFAULT_DL_PATH = "temp/datalake";

	public static Archchess instance() {
		return INSTANCE;
	}

	private File home;
	private int port;
	private String databasePath = DEFAULT_DB_PATH;
	private String datalakePath = DEFAULT_DL_PATH;

	private ChessAPI api;
	private Database database;
	private EventPublisher eventPublisher;
	private EventReader eventReader;
	private PluginsManager pluginsManager;

	public File home() {
		return home;
	}

	public String databasePath() {
		return databasePath;
	}

	public Archchess databasePath(String databasePath) {
		this.databasePath = databasePath;
		return this;
	}

	public Archchess home(File home) {
		this.home = home;
		return this;
	}

	public int port() {
		return port;
	}

	public Archchess port(int port) {
		this.port = port;
		return this;
	}

	public ChessAPI api() {
		return api;
	}

	public PluginsManager getPluginsManager() {
		return pluginsManager;
	}

	public EventReader eventReader() {
		return eventReader;
	}

	public synchronized void publish(Event event) {
		eventPublisher.publish(event);
	}

	public void launch() {
		api = new ChessAPI(home, port);
		database = new Database(databasePath);
		eventPublisher = new EventPublisher(datalakePath);
		eventReader = new EventReader(datalakePath);
		pluginsManager = new PluginsManager(home);

		Thread thread = new Thread(() -> {
			pluginsManager.reloadPlugins();
            try {
                Thread.sleep(Duration.ofMinutes(3));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
		thread.setDaemon(true);
		thread.start();

		api.start();
		database.init();
	}

	public Database database() {
		return database;
	}
}
