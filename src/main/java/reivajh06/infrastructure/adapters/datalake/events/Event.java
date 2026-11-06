package reivajh06.infrastructure.adapters.datalake.events;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Event {

	private final String timestamp;
	private final String id;
	private final String username;

	public Event(String username) {
		this.timestamp = LocalDateTime.now().toString();
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
