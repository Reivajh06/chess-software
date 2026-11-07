package reivajh06.infrastructure;

import reivajh06.architecture.domain.model.game.GameSession;
import reivajh06.architecture.ports.database.UserDatabaseClient;
import reivajh06.infrastructure.adapters.Sessions;
import reivajh06.infrastructure.adapters.datalake.EventPublisher;
import reivajh06.infrastructure.adapters.datalake.EventReader;
import reivajh06.infrastructure.adapters.datalake.events.Event;
import reivajh06.infrastructure.adapters.plugins.PluginsManager;
import reivajh06.infrastructure.adapters.sqlite.SqliteDatabaseClient;
import reivajh06.infrastructure.adapters.web.ChessAPI;

import java.io.File;
import java.util.UUID;

public class Archchess {

	private static final Archchess INSTANCE = new Archchess();

	public static Archchess instance() {
		return INSTANCE;
	}

	private final File home;
	private final int port;
	private final String databasePath;
	private final String datalakePath;
	private final String pluginsPath;

	private ChessAPI api;
	private UserDatabaseClient userDatabaseClient;
	private EventPublisher eventPublisher;
	private EventReader eventReader;
	private PluginsManager pluginsManager;

	private Archchess() {
		Configuration configuration = Configuration.loadConfigFile();

		home = new File(configuration.homePath());
		datalakePath = configuration.datalakePath();
		databasePath = configuration.databasePath();
		port = configuration.apiPort();
		pluginsPath = configuration.pluginsPath();
	}

	public void launch() {
		new File(databasePath).getAbsoluteFile().getParentFile().mkdirs();
		new File(datalakePath).mkdirs();
		new File(pluginsPath).mkdirs();

		api = new ChessAPI(home, port);
		userDatabaseClient = new SqliteDatabaseClient(databasePath);
		eventPublisher = new EventPublisher(datalakePath);
		eventReader = new EventReader(datalakePath);
		pluginsManager = new PluginsManager(pluginsPath);
		pluginsManager.configurePluginsReload();

		api.start();
		userDatabaseClient.init();
	}

	public PluginsManager getPluginsManager() {
		return pluginsManager;
	}

	public synchronized void publish(Event event) {
		eventPublisher.publish(event);
	}

	public void registerSession(GameSession session) {
		Sessions.registerSession(session);
	}

	public GameSession session(UUID sessionId) {
		return Sessions.fetchSession(sessionId);
	}

	public UserDatabaseClient database() {
		return userDatabaseClient;
	}
}
