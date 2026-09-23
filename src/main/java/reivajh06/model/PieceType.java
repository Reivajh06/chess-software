package reivajh06.model;

public enum PieceType {

	PAWN("♙"),
	ROOK("♖"),
	KNIGHT("♘"),
	KING("♔"),
	QUEEN("♕"),
	BISHOP("♗");

	final String sprite;

	PieceType(String sprite) {
		this.sprite = sprite;
	}
}
