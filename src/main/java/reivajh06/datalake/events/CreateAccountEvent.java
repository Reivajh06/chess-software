package reivajh06.datalake.events;

public class CreateAccountEvent extends Event {

	public CreateAccountEvent(String username) {
		super(username);
	}
}
