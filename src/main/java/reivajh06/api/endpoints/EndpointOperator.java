package reivajh06.api.endpoints;

import reivajh06.api.EndpointContext;

public interface EndpointOperator<I, O> {

	O run(EndpointContext ctx, I input);

	I deserializeBody(String body);
}
