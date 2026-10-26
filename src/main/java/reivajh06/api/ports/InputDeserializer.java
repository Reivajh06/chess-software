package reivajh06.api.ports;

import java.util.Map;

@FunctionalInterface
public interface InputDeserializer<I> {

	I deserialize(Map<String, String> pathParams, String body);
}
