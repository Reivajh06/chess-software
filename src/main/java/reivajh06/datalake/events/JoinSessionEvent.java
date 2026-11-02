package reivajh06.datalake.events;

public class JoinSessionEvent extends Event {

	private final String sessionId;
	private final String rivalUsername;

	public JoinSessionEvent(String username, String sessionId, String rivalUsername) {
		super(username);
		this.sessionId = sessionId;
		this.rivalUsername = rivalUsername;
	}
}
