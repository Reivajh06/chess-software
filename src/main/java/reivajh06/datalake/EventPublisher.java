package reivajh06.datalake;

import reivajh06.Json;
import reivajh06.datalake.events.Event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;

public class EventPublisher {

	private final String datalakePath;

	public EventPublisher(String datalakePath) {
		this.datalakePath = datalakePath;
	}

	public void publish(Event event) {
		try {
			File eventFolder = new File(datalakePath + "/" + event.getClass().getSimpleName());
			eventFolder.mkdirs();

			File target = new File(eventFolder + "/" + LocalDateTime.parse(event.timestamp()).toLocalDate().toString() + ".jsonl");

			Files.writeString(target.toPath(), Json.toJson(event) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);

		} catch (IOException e) {
			throw new PublisherException("Error while publishing event: %s".formatted(event), e);
		}
	}
}
