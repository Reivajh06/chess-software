package reivajh06.datalake.events;

import java.util.UUID;

public class CreateSessionEvent extends Event {

	private final String sessionId;

	public CreateSessionEvent(String username, UUID sessionId) {
		super(username);
		this.sessionId = sessionId.toString();
	}
}
