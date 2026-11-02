package reivajh06.datalake.events;

public class LogoutEvent extends Event {

	public LogoutEvent(String username) {
		super(username);
	}
}
