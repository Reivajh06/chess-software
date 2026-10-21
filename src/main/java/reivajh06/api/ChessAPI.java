package reivajh06.api;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import reivajh06.api.endpoints.*;

public class ChessAPI {

	public static final int PORT = 5000;

	private Javalin server;

	public ChessAPI() {
		server = Javalin.create(this::configureEndpoints);

		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

	public void start() {
		server.start(PORT);
	}

	public void configureEndpoints(JavalinConfig config) {
		config.staticFiles.add("/web", Location.CLASSPATH);

		config.routes.post("/api/sessions", new PostCreateSession());
		config.routes.post("/api/sessions/{sessionId}/legal-moves", new PostLegalMoves());
		config.routes.post("/api/sessions/{sessionId}/moves", new PostMovePiece());
		config.routes.get("/api/sessions/{sessionId}", new GetReadCurrentState());
		config.routes.get("/api/server", new GetServerAddress());
		config.routes.get("/api/sessions/{sessionId}/promote", new GetServerAddress());
		config.routes.post("/api/sessions/{sessionId}/join", new PostJoinSession());
		config.routes.post("/api/sessions/{sessionId}/promote", new PostPromotePiece());
	}
}
