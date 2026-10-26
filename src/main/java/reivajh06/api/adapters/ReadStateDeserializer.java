package reivajh06.api.adapters;

import reivajh06.api.ports.InputDeserializer;

import java.util.Map;
import java.util.UUID;

public class ReadStateDeserializer implements InputDeserializer<UUID> {

	@Override
	public UUID deserialize(Map<String, String> pathParams, String body) {
		return UUID.fromString(pathParams.get("sessionId"));
	}
}
