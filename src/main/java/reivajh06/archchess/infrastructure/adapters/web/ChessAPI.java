package reivajh06.archchess.infrastructure.adapters.web;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.*;

import java.io.File;

public class ChessAPI {

	private final Javalin server;
	private final int port;
	private final File home;

	public ChessAPI(File home, int port) {
		this.home = home;
		server = Javalin.create(this::configureEndpoints);
		this.port = port;

		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

	public void start() {
		server.start(port);
	}

	public void configureEndpoints(JavalinConfig config) {
		config.staticFiles.add("/web", Location.CLASSPATH);

		config.routes.post("/api/sessions", c -> EndpointHandler.of(new PostCreateSession()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/legal-moves", c -> EndpointHandler.of(new PostLegalMoves()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/moves", c -> EndpointHandler.of(new PostMovePiece()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/join", c -> EndpointHandler.of(new PostJoinSession()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/accounts", c -> EndpointHandler.of(new PostCreateAccount()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/login", c -> EndpointHandler.of(new PostLogin()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/logout", c -> EndpointHandler.of(new PostLogout()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/promote", c -> EndpointHandler.of(new PostPromotePiece()).handle(JavalinEndpointContext.of(c)));

		config.routes.get("/api/sessions/{sessionId}", c -> EndpointHandler.of(new GetReadCurrentState()).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/server", c -> EndpointHandler.of(new GetServerAddress(port)).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/plugins", c -> EndpointHandler.of(new GetAvailablePlugins()).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/history", c -> EndpointHandler.of(new GetSessionsHistory()).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/sessions/{sessionId}/request-move", c -> EndpointHandler.of(new GetRequestMove()).handle(JavalinEndpointContext.of(c)));

		config.routes.delete("/api/accounts/delete", c -> EndpointHandler.of(new DeleteAccount()).handle(JavalinEndpointContext.of(c)));
	}
}
