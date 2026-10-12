package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import reivajh06.Json;

import java.util.Map;

public class PostCreateSession implements Handler {

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		Map<String, String> sessionType = Json.fromJson(ctx.body(), Type.getType());
	}
}
