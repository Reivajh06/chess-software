package reivajh06;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import reivajh06.game_logic.GameSession;
//import reivajh06.endpoints.*;

public class ChessAPI {

	private static final int PORT = 5000;
	private static GameSession gameSession;
	private Javalin server;

	public ChessAPI() {
		server = Javalin.create(ChessAPI.this::configureEndpoints);

		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

	public void start() {
		server.start(PORT);
	}

	public void configureEndpoints(JavalinConfig config) {
		//Creates get endpoints for every file inside web folder
		config.staticFiles.add("/web", Location.CLASSPATH);
	}
}
