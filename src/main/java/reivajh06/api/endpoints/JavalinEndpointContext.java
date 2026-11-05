package reivajh06.api.endpoints;

import io.javalin.http.Context;
import reivajh06.api.EndpointContext;

import java.util.Map;

public class JavalinEndpointContext implements EndpointContext {

    private final Context ctx;

    public JavalinEndpointContext(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public Map<String, String> pathParamMap() {
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
    public void status(int statusCode) {
        ctx.status(statusCode);
    }

    @Override
    public void response(String body) {
        ctx.result(body);
    }
}
