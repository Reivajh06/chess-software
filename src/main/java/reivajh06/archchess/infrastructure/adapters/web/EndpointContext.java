package reivajh06.archchess.infrastructure.adapters.web;

import reivajh06.archchess.architecture.domain.model.Player;

import java.util.List;
import java.util.Map;

public interface EndpointContext {

    Player sessionPlayer();

    String sessionId();

    Map<String, String> pathParams();

    Map<String, List<String>> queryParams();

    String body();

    void invalidateSession();

    void sessionAttribute(String name, Object value);

    <V> V sessionAttribute(String name);

    void status(HttpStatus httpStatus);

    void response(String body);
}
