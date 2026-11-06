package reivajh06.infrastructure.adapters.datalake;

import reivajh06.infrastructure.adapters.Json;
import reivajh06.infrastructure.adapters.datalake.events.Event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class EventReader {

	private final String datalakePath;

	public EventReader(String datalakePath) {
		this.datalakePath = datalakePath;
	}

	public List<? extends Event> events(Class<? extends Event> eventClass) {
		File[] files = new File(datalakePath + "/" + eventClass.getSimpleName()).listFiles();

		if(files == null) return new ArrayList<>();

		return Arrays.stream(files)
				.map(File::toPath)
				.map(path -> {
					try {
						return Files.readAllLines(path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.flatMap(List::stream)
				.map(json -> (Event) Json.fromJson(json, eventClass))
				.toList();
	}

	public List<? extends Event> eventsByUsername(Class<? extends Event> eventClass, String username) {
		return events(eventClass).stream()
				.filter(e -> e.username().equals(username))
				.toList();
	}

	public <E extends Event> List<E> eventsBy(Class<E> eventClass, Predicate<E> filter) {
		return events(eventClass).stream()
				.map(eventClass::cast)
				.filter(filter::test)
				.toList();
	}
}
