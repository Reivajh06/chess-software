package reivajh06.api;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import reivajh06.api.endpoints.*;

import java.io.File;

public class ChessAPI {

	private Javalin server;
	private int port;
	private final File home;

	public ChessAPI(File home, int port) {
		this.home = home;
		server = Javalin.create(this::configureEndpoints);
		this.port = port;

		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

	public File home() {
		return home;
	}

	public void start() {
		server.start(port);
	}

	public int port() {
		return port;
	}

	public void configureEndpoints(JavalinConfig config) {
		config.staticFiles.add("/web", Location.CLASSPATH);

		config.routes.post("/api/sessions", new PostCreateSession());
		config.routes.post("/api/sessions/{sessionId}/legal-moves", new PostLegalMoves());
		config.routes.post("/api/sessions/{sessionId}/moves", new PostMovePiece());
		config.routes.get("/api/sessions/{sessionId}", new GetReadCurrentState());
		config.routes.get("/api/server", new GetServerAddress(port));
		config.routes.get("/api/sessions/{sessionId}/promote", new PostPromotePiece());
		config.routes.post("/api/sessions/{sessionId}/join", new PostJoinSession());
		config.routes.post("/api/accounts", new PostCreateAccount());
	}
}
