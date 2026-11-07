package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.dtos.AvailablePluginsDto;

public class GetAvailablePlugins implements EndpointOperator<Void, AvailablePluginsDto> {

    @Override
    public AvailablePluginsDto run(EndpointContext ctx, Void input) {
        return new AvailablePluginsDto(
                Archchess.instance().getPluginsManager().pluginNames()
        );
    }

    @Override
    public Void deserializeBody(String body) {
        return null;
    }
}
