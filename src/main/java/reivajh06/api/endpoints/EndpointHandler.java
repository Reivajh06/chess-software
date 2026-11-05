package reivajh06.api.endpoints;

import reivajh06.api.ApiException;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.services.Json;

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
		}
	}
}
