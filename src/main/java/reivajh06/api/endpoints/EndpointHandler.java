package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import reivajh06.Json;
import reivajh06.api.ApiException;

import java.util.Map;

public abstract class EndpointHandler<I, O> implements Handler {

	@Override
	public void handle(Context ctx) {
		try {
			I input = deserializeBody(ctx);
			O output = run(ctx, ctx.pathParamMap(), input);

			if(output == null) return;

			ctx.result(Json.toJsonPretty(output));

		} catch(ApiException e) {
			ctx.status(e.status());
		}
	}

	protected abstract O run(Context ctx, Map<String, String> pathParamMap, I input);

	protected abstract I deserializeBody(Context ctx);
}
