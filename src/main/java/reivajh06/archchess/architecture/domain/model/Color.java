package reivajh06.archchess.architecture.domain.model;

public enum Color {

	WHITE,
	BLACK;

	public static Color rivalColor(Color color) {
		return color == BLACK ? WHITE : BLACK;
	}

	public static Color tileColor(Tile tile) {
		Color rankStartingColor = tile.rank().number % 2 == 0 ? WHITE : BLACK;

		if(rankStartingColor == BLACK) return tile.file().code % 2 == 0 ? WHITE : BLACK;

		else return tile.file().code % 2 == 0 ? BLACK : WHITE;
	}
}
