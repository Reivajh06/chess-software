package reivajh06.api.endpoints;

import io.javalin.http.Context;
import reivajh06.Archchess;
import reivajh06.datalake.events.LogoutEvent;

import java.util.Map;

public class PostLogout extends EndpointHandler<Void, Void> {

	@Override
	protected Void run(Context ctx, Map<String, String> pathParamMap, Void input) {
		Archchess.instance().publish(new LogoutEvent(ctx.sessionAttribute("username")));
		ctx.req().getSession().invalidate();
		return null;
	}

	@Override
	protected Void deserializeBody(Context ctx) {
		return null;
	}
}
