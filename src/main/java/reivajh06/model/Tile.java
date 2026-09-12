package reivajh06.model;

import org.jetbrains.annotations.NotNull;

public record Tile(String col, int row) {

	public String name() {
		return col + row;
	}

	@NotNull
	@Override
	public String toString() {
		return name();
	}
}
