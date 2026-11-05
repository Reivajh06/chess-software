package reivajh06.api;

import java.util.Map;

public interface EndpointContext {

    Map<String, String> pathParamMap();

    String body();

    void invalidateSession();

    void sessionAttribute(String name, Object value);

    <V> V sessionAttribute(String name);

    void status(int httpStatus);

    void response(String body);
}
