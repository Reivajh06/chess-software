package reivajh06.model;

import org.jetbrains.annotations.NotNull;

public record Tile(File col, int row) {

	public Tile(String name) {
		this(File.valueOf(name.substring(0, 1).toUpperCase()), name.charAt(1) - '0');
	}

	public String name() {
		return col.name() + row;
	}

	@NotNull
	@Override
	public String toString() {
		return name();
	}
}
