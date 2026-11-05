package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import reivajh06.services.Json;
import reivajh06.api.ApiException;

import java.util.Map;

public abstract class EndpointHandler<I, O> implements Handler  {

	public void handle(Context ctx) {
		try {
			I input = deserializeBody(ctx.body());
			O output = run(ctx, ctx.pathParamMap(), input);

			if(output == null) return;

			ctx.result(Json.toJsonPretty(output));

		} catch(ApiException e) {
			ctx.status(e.status());
		}
	}

	abstract O run(Context ctx, Map<String, String> pathParamMap, I input);

	abstract I deserializeBody(String body);
}
