package reivajh06.archchess.infrastructure.adapters.datalake.events;

public class RequestMoveEvent extends Event {

	public RequestMoveEvent(String username) {
		super(username);
	}
}
