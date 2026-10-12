package reivajh06.api;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import reivajh06.api.endpoints.PostCreateSession;
import reivajh06.api.endpoints.PostLegalMoves;

public class ChessAPI {

	private static final int PORT = 5000;

	private Javalin server;

	public ChessAPI() {
		server = Javalin.create(ChessAPI.this::configureEndpoints);

		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

	public void start() {
		server.start(PORT);
	}

	public void configureEndpoints(JavalinConfig config) {
		config.staticFiles.add("/web", Location.CLASSPATH);

		config.routes.post("/api/sessions", new PostCreateSession());
		config.routes.post("/api/sessions/{sessionId}/legal-moves", new PostLegalMoves());
	}
}
