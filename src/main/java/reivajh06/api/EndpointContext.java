package reivajh06.api;

import java.util.Map;

public interface EndpointContext {

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
