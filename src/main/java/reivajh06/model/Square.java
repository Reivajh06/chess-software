package reivajh06.model;

import org.jetbrains.annotations.NotNull;

public record Square(BoardColumns col, int row) {

	public Square(String name) {
		this(BoardColumns.valueOf(name.substring(0, 1).toUpperCase()), name.charAt(1) - '0');
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
