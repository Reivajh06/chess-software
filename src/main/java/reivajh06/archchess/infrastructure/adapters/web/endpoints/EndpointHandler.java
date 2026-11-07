package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;

import java.util.Map;

public class EndpointHandler<I, O> {

	public static <I, O> EndpointHandler<I, O> of(EndpointOperator<I, O> operator) {
		return new EndpointHandler<>(operator);
	}

	private final EndpointOperator<I, O> operator;

	private EndpointHandler(EndpointOperator<I, O> operator) {
		this.operator = operator;
	}

	public void handle(EndpointContext ctx) {
		try {
			I input = operator.deserializeBody(ctx.body());
			O output = operator.run(ctx, input);

			if(output == null) return;

			ctx.status(HttpStatus.OK);
			ctx.response(Json.toJsonPretty(output));

		} catch(ApiException e) {
			ctx.status(e.status());
			ctx.response(Json.toJson(Map.of(
					"error", e.status().name(),
					"message",  e.getMessage()
			)));

		} catch(Exception e) {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.response(Json.toJson(Map.of(
					"error", HttpStatus.BAD_REQUEST.name(),
					"message",  "Unexpected exception occurred while processing request"
			)));
		}
	}
}
