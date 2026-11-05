package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.datalake.events.LogoutEvent;

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
