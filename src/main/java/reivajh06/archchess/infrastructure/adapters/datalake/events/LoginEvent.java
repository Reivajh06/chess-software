package reivajh06.archchess.infrastructure.adapters.datalake.events;

public class LoginEvent extends Event {

	public LoginEvent(String username) {
		super(username);
	}
}
