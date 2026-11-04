package reivajh06.api.endpoints;

import io.javalin.http.Context;
import reivajh06.Archchess;
import reivajh06.api.dtos.AvailablePluginsDto;

import java.util.Map;

public class GetAvailablePlugins extends EndpointHandler<Void, AvailablePluginsDto> {

    @Override
    protected AvailablePluginsDto run(Context ctx, Map<String, String> pathParamMap, Void input) {
        return new AvailablePluginsDto(
                Archchess.instance().getPluginsManager().pluginNames()
        );
    }

    @Override
    protected Void deserializeBody(String body) {
        return null;
    }
}
