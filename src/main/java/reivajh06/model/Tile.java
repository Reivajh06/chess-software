package reivajh06.model;

import org.jetbrains.annotations.NotNull;

public record Tile(String col, int row) {

	public Tile(String name) {
		this(name.substring(0, 1), name.charAt(1) - '0');
	}

	public String name() {
		return col + row;
	}

	@NotNull
	@Override
	public String toString() {
		return name();
	}
}
