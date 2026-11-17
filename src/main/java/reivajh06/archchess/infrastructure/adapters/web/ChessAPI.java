package reivajh06.archchess.infrastructure.adapters.web;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.*;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.get.*;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.post.*;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.delete.*;

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

		config.routes.post("/api/sessions", c -> EndpointHandler.of(new CreateSessionOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/legal-moves", c -> EndpointHandler.of(new LegalMovesOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/moves", c -> EndpointHandler.of(new MovePieceOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/join", c -> EndpointHandler.of(new JoinSessionOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/accounts", c -> EndpointHandler.of(new CreateAccountOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/login", c -> EndpointHandler.of(new LoginOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/logout", c -> EndpointHandler.of(new LogoutOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.post("/api/sessions/{sessionId}/promote", c -> EndpointHandler.of(new PromotePieceOperator()).handle(JavalinEndpointContext.of(c)));

		config.routes.get("/api/sessions/{sessionId}", c -> EndpointHandler.of(new ReadCurrentStateOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/server", c -> EndpointHandler.of(new ServerAddressOperator(port)).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/plugins", c -> EndpointHandler.of(new AvailablePluginsOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/history", c -> EndpointHandler.of(new SessionsHistoryOperator()).handle(JavalinEndpointContext.of(c)));
		config.routes.get("/api/sessions/{sessionId}/request-move", c -> EndpointHandler.of(new RequestMoveOperator()).handle(JavalinEndpointContext.of(c)));

		config.routes.delete("/api/accounts/delete", c -> EndpointHandler.of(new DeleteAccount()).handle(JavalinEndpointContext.of(c)));
		config.routes.delete("/api/sessions/{sessionId}", c -> EndpointHandler.of(new DeleteSession()).handle(JavalinEndpointContext.of(c)));

		configureAlfiEndpoints(config);
	}

	private void configureAlfiEndpoints(JavalinConfig config) {
		config.routes.post("/api/sessions/{sessionId}/alfi/invert-seats", c -> EndpointHandler.of(new InvertSeatsOperator()).handle(JavalinEndpointContext.of(c)));
	}
}
