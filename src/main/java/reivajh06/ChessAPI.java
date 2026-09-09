package reivajh06;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;

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
		config.staticFiles.add("/web", Location.CLASSPATH);
		loadWebPage(config);
	}

	private void loadWebPage(JavalinConfig config) {

		/**configRoutes.get("/", ctx -> {
			ctx.contentType("text/html");
			ctx.result(Files.readString(new File("web/static/chatbot.html").toPath()));
		});

		server.get("/resource/{type}", ctx -> {
			String type = ctx.pathParam("type");
			if(type.equalsIgnoreCase("css")) {
				ctx.contentType("text/css");
			} else {
				ctx.contentType("text/javascript");
			}
			ctx.result(Files.readString(new File("web/static/chatbot." + type).toPath()));
		});**/
	}
}
