package reivajh06;

import reivajh06.api.ChessAPI;
import reivajh06.database.Database;

import java.io.File;

public class Archchess {

	private static final Archchess INSTANCE = new Archchess();
	private static final String DEFAULT_DB_PATH = "temp/archchess.db";

	public static Archchess instance() {
		return INSTANCE;
	}

	private File home;
	private int port;
	private ChessAPI api;
	private Database database;
	private String databasePath = DEFAULT_DB_PATH;

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

	public void launch() {
		api = new ChessAPI(home, port);
		database = new Database(databasePath);

		api.start();
		database.init();
	}

	public Database database() {
		return database;
	}
}
