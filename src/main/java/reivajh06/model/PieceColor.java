package reivajh06.model;

public enum PieceColor {

	WHITE,
	BLACK;

	public static PieceColor rivalColor(PieceColor color) {
		return color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
	}
}
