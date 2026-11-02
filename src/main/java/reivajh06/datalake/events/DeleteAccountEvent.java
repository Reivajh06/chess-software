package reivajh06.datalake.events;

public class DeleteAccountEvent extends Event {

	public DeleteAccountEvent(String username) {
		super(username);
	}
}
