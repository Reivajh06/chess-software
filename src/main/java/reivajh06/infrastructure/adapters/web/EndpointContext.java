package reivajh06.infrastructure.adapters.web;

import reivajh06.architecture.domain.model.Player;

import java.util.Map;

public interface EndpointContext {

    Player sessionPlayer();

    String ip();

    String sessionId();

    Map<String, String> pathParams();

    String body();

    void invalidateSession();

    void sessionAttribute(String name, Object value);

    <V> V sessionAttribute(String name);

    void status(HttpStatus httpStatus);

    void response(String body);
}
