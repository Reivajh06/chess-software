package reivajh06.model;

import org.jetbrains.annotations.NotNull;

public record Tile(File file, Rank rank) {

	public Tile(String name) {
		this(File.valueOf(name.substring(0, 1).toUpperCase()), Rank.values()[name.charAt(1) - '0' - 1]);
	}

	public String name() {
		return file().name().charAt(0) + rank.name().substring(0, 1);
	}

	@NotNull
	@Override
	public String toString() {
		return name();
	}
}
