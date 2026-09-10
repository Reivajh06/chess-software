package reivajh06.model;

import java.util.Locale;

public record Piece(PieceType type, PieceColor color) {

	public String getCode() {
		return color.name().substring(0, 1).toLowerCase() + type.name().substring(0, 1).toLowerCase();
	}
}
