package reivajh06.datalake.events;

import java.time.LocalDate;
import java.util.UUID;

public abstract class Event {

	private final String timestamp;
	private final String id;
	private final String username;

	public Event(String username) {
		this.timestamp = LocalDate.now().toString();
		this.id = UUID.randomUUID().toString();
		this.username = username;
	}

	public String timestamp() {
		return timestamp;
	}

	public String id() {
		return id;
	}

	public String username() {
		return username;
	}

	@Override
	public String toString() {
		return "Event{" +
				"timestamp=" + timestamp +
				", id='" + id + '\'' +
				", username='" + username + '\'' +
				'}';
	}
}
