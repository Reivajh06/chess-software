package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.datalake.events.LogoutEvent;

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
