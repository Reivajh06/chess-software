package reivajh06.model;

public enum PieceColor {

	WHITE,
	BLACK;

	public static PieceColor rivalColor(PieceColor color) {
		return color == BLACK ? WHITE : BLACK;
	}

	public static PieceColor tileColor(Tile tile) {
		PieceColor rankStartingColor = tile.rank().number % 2 == 0 ? WHITE : BLACK;

		if(rankStartingColor == BLACK) return tile.file().code % 2 == 0 ? WHITE : BLACK;

		else return tile.file().code % 2 == 0 ? BLACK : WHITE;
	}
}
