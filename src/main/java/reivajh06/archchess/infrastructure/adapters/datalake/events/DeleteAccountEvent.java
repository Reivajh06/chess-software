package reivajh06.archchess.infrastructure.adapters.datalake.events;

public class DeleteAccountEvent extends Event {

	public DeleteAccountEvent(String username) {
		super(username);
	}
}
