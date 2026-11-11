package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.dtos.AvailablePlugins;

public class GetAvailablePlugins implements EndpointOperator<Void, AvailablePlugins> {

    @Override
    public AvailablePlugins run(EndpointContext ctx, Void input) {
        return new AvailablePlugins(
                Archchess.instance().pluginNames()
        );
    }

    @Override
    public Void deserializeBody(String body) {
        return null;
    }
}
