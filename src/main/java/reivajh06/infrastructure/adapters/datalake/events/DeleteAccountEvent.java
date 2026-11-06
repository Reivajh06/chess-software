package reivajh06.infrastructure.adapters.datalake.events;

public class DeleteAccountEvent extends Event {

	public DeleteAccountEvent(String username) {
		super(username);
	}
}
