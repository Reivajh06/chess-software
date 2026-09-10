package reivajh06.model;

import org.jetbrains.annotations.NotNull;

public record Piece(PieceType type, PieceColor color) {

	public String getCode() {
		return color.name().substring(0, 1).toLowerCase() + type.name().substring(0, 1).toLowerCase();
	}

	public PieceColor getColor() {
		return color;
	}

	public PieceType getType() {
		return type;
	}

	@NotNull
	@Override
	public String toString() {
		return getCode();
	}
}
