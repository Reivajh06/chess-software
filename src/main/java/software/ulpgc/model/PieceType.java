package software.ulpgc.model;

public enum PieceType {

	PAWN("P"),
	ROOK("R"),
	KNIGHT("N"),
	KING("K"),
	QUEEN("Q"),
	BISHOP("B");

	public final String firstLetter;

	PieceType(String firstLetter) {
		this.firstLetter = firstLetter;
	}
}
