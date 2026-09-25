import reivajh06.gamelogic.Difficulty;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameStatus;
import reivajh06.gamelogic.Gamemode;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

void main() {
	Scanner scanner = new Scanner(System.in);

	GameSession game = new GameSession(
			Gamemode.PVP,
			Difficulty.NOT_CPU
	);

	// GameSession will print the board after every successful move.
	game.setDebug(true);

	IO.println("=== CHESS SIMULATION ===");
	IO.println("Enter tiles using chess notation: e2, e4, a8...");
	IO.println("Type 'quit' to stop the game.");
	IO.println();

	game.printBoard();

	while (game.status() == GameStatus.ACTIVE) {
		IO.println("Turn: " + game.colorTurn());

		Tile from = readTile(scanner, "Select piece: ");

		if (from == null)
			break;

		Map<Move, List<Tile>> possibleMoves = game.pieceMovesetTiles(from);

		List<Tile> possibleTiles = possibleMoves.values()
				.stream()
				.flatMap(List::stream)
				.distinct()
				.toList();

		if (possibleTiles.isEmpty()) {
			IO.println("That piece cannot be moved.");
			IO.println();
			continue;
		}

		IO.println("Possible moves: " + possibleTiles);

		Tile to = readTile(scanner, "Move to: ");

		if (to == null)
			break;

		Optional<Move> selectedMove = possibleMoves.entrySet()
				.stream()
				.filter(entry -> entry.getValue().contains(to))
				.map(Map.Entry::getKey)
				.findFirst();

		if (selectedMove.isEmpty()) {
			IO.println("Invalid move.");
			IO.println();
			continue;
		}

		game.move(
				selectedMove.get(),
				from,
				to
		);
	}

	if (game.status() == GameStatus.WHITE_WIN) {
		IO.println("WHITE WINS!");
	} else if (game.status() == GameStatus.BLACK_WIN) {
		IO.println("BLACK WINS!");
	} else {
		IO.println("Game ended.");
	}

	scanner.close();
}

private static Tile readTile(Scanner scanner, String message) {
	while (true) {
		IO.print(message);

		String input = scanner.nextLine()
				.trim()
				.toLowerCase();

		if (input.equals("quit"))
			return null;

		if (!input.matches("[a-h][1-8]")) {
			IO.println("Invalid tile. Example: e2");
			continue;
		}

		return new Tile(input);
	}
}