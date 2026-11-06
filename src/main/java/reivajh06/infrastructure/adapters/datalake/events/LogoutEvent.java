package reivajh06.infrastructure.adapters.datalake.events;

public class LogoutEvent extends Event {

	public LogoutEvent(String username) {
		super(username);
	}
}
