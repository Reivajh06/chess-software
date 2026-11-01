package reivajh06.datalake;

import reivajh06.Json;
import reivajh06.datalake.events.Event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class EventPublisher {

	private final String datalakePath;

	public EventPublisher(String datalakePath) {
		this.datalakePath = datalakePath;
	}

	public void publish(Event event) {
		try {
			File eventFolder = new File(datalakePath + "/" + event.getClass().getSimpleName().toLowerCase());
			eventFolder.mkdirs();

			File target = new File(eventFolder + "/" + event.timestamp().toString() + ".jsonl");

			Files.writeString(target.toPath(), Json.toJson(event) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);

		} catch (IOException e) {
			throw new PublisherException("Error while publishing event: %s".formatted(event), e);
		}
	}
}
