package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import reivajh06.Json;
import reivajh06.api.ApiException;

public abstract class EndpointHandler<O> implements Handler {

	@Override
	public void handle(Context ctx) {
		try {
			O output = run(ctx);
			ctx.result(Json.toJson(output));

		} catch(ApiException e) {
			ctx.status(e.errorCode());
		}
	}

	protected abstract O run(Context ctx);
}
