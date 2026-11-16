package reivajh06.archchess.infrastructure.adapters.web.endpoints.post;

import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.datalake.events.LogoutEvent;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.EndpointOperator;

public class PostLogout implements EndpointOperator<Void, Void> {

	@Override
	public Void run(EndpointContext ctx, Void input) {
		Archchess.instance().publish(new LogoutEvent(ctx.sessionAttribute("username")));
		ctx.invalidateSession();
		return null;
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
