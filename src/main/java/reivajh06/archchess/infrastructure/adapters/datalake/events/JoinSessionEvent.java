package reivajh06.archchess.infrastructure.adapters.datalake.events;

public class JoinSessionEvent extends Event {

	private final String sessionId;
	private final String rivalUsername;

	public JoinSessionEvent(String username, String sessionId, String rivalUsername) {
		super(username);
		this.sessionId = sessionId;
		this.rivalUsername = rivalUsername;
	}

	public String sessionId() {
		return sessionId;
	}

	public String rivalUsername() {
		return rivalUsername;
	}
}
