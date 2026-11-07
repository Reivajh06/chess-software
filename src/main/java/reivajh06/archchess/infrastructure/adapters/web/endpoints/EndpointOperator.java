package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;

public interface EndpointOperator<I, O> {

	O run(EndpointContext ctx, I input);

	I deserializeBody(String body);
}
