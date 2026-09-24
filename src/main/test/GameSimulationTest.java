import reivajh06.gamelogic.Difficulty;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.Gamemode;
import reivajh06.model.Tile;

void main() {
	GameSession gameSession = new GameSession(Gamemode.PVAI, Difficulty.NOT_CPU);
	gameSession.setDebug(true);

	Scanner scanner = new Scanner(System.in);

	System.out.println("Specify tiles with their name [Example: 'a2']");
	System.out.println("Type 'q' at any moment to exit.");

	while (true) {
		gameSession.printBoard();

		System.out.printf("==== %s TURN ====%n", gameSession.colorTurn());

		// =========================
		// SELECT PIECE
		// =========================

		System.out.print("Select piece: ");
		String fromInput = scanner.nextLine().trim().toLowerCase();

		if (fromInput.equals("q")) {
			break;
		}

		if (!fromInput.matches("[abcdefgh][1-8]")) {
			System.out.println("Invalid tile.");
			continue;
		}

		Tile from = new Tile(fromInput);

		List<Tile> possibleMoves = gameSession.pieceMovesetTiles(from);

		if (possibleMoves.isEmpty()) {
			System.out.println("This piece has no possible moves.");
			continue;
		}

		System.out.println("Possible moves:");

		for (Tile tile : possibleMoves) {
			System.out.println(" - " + tile);
		}

		// =========================
		// SELECT DESTINATION
		// =========================

		System.out.print("Move to: ");
		String toInput = scanner.nextLine().trim().toLowerCase();

		if (toInput.equals("q")) {
			break;
		}

		if (!toInput.matches("[abcdefgh][1-8]")) {
			System.out.println("Invalid tile.");
			continue;
		}

		Tile to = new Tile(toInput);

		if (!possibleMoves.contains(to)) {
			System.out.println("The selected destination is not a possible move.");
			continue;
		}

		// =========================
		// EXECUTE MOVE
		// =========================

		gameSession.move(from, to);

		System.out.println();
	}

	scanner.close();
}
