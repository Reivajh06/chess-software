package reivajh06.infrastructure.adapters.web;

import io.javalin.http.Context;
import reivajh06.architecture.domain.model.Player;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.web.HttpStatus;

import java.util.Map;

public class JavalinEndpointContext implements EndpointContext {

    public static JavalinEndpointContext of(Context ctx) {
        return new JavalinEndpointContext(ctx);
    }

    private final Context ctx;

    private JavalinEndpointContext(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public Player sessionPlayer() {
        return ctx.sessionAttribute("username") == null ?
                Player.guest(ctx.req().getSession().getId())
                : new Player(ctx.sessionAttribute("username"), "");
    }

    @Override
    public String ip() {
        return ctx.ip();
    }

    @Override
    public String sessionId() {
        return ctx.req().getSession().getId();
    }

    @Override
    public Map<String, String> pathParams() {
        return ctx.pathParamMap();
    }

    @Override
    public String body() {
        return ctx.body();
    }

    @Override
    public void invalidateSession() {
        ctx.req().getSession().invalidate();
    }

    @Override
    public void sessionAttribute(String name, Object value) {
        ctx.sessionAttribute(name, value);
    }

    @Override
    public <V> V sessionAttribute(String name) {
        return ctx.sessionAttribute(name);
    }

    @Override
    public void status(HttpStatus httpStatus) {
        ctx.status(httpStatus.code);
    }

    @Override
    public void response(String body) {
        ctx.result(body);
    }
}
