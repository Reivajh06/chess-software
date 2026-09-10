package reivajh06;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import reivajh06.endpoints.*;

public class ChessAPI {

	private final int PORT = 7070;
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

		config.routes.get("/api/game-options", new GetGameOptions());
		config.routes.post("/api/games", new PostCreateGame());
		config.routes.get("/api/games/{gameId}", new GetGameStatus());
		config.routes.get("/api/games/{gameId}/legal-moves?from={square}", new GetLegalMoves());
		config.routes.post("/api/games/{gameId}/moves", new PostSubmitMove());
		config.routes.post("/api/games/{gameId}/turns/advance", new PostNextMove());
	}
}
