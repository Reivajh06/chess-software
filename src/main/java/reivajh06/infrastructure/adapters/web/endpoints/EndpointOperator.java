package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.infrastructure.adapters.web.EndpointContext;

public interface EndpointOperator<I, O> {

	O run(EndpointContext ctx, I input);

	I deserializeBody(String body);
}
