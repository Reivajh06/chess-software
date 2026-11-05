package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.api.dtos.AvailablePluginsDto;

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
