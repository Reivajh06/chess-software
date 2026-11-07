package reivajh06.archchess.infrastructure.adapters.datalake.events;

public class CreateAccountEvent extends Event {

	public CreateAccountEvent(String username) {
		super(username);
	}
}
