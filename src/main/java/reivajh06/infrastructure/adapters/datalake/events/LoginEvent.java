package reivajh06.infrastructure.adapters.datalake.events;

public class LoginEvent extends Event {

	public LoginEvent(String username) {
		super(username);
	}
}
