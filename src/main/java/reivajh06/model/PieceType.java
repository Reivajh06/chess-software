package reivajh06.model;

import java.util.Arrays;

public enum PieceType {

	PAWN,
	ROOK,
	KNIGHT,
	KING,
	QUEEN,
	BISHOP;

	public static boolean isClassicType(String typeName) {
		return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(typeName)).findFirst().orElse(null) != null;
	}
}
