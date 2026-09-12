import reivajh06.model.Difficulty;
import reivajh06.model.Game;
import reivajh06.model.Gamemode;
import reivajh06.model.Tile;

public class GameTest {

	public static void main(String[] args) {
		Game game = new Game(Gamemode.PVAI, Difficulty.NOT_CPU);

		// 1. Pawns — test initial two-square movement
		move(game, "e2", "e4"); // White pawn
		move(game, "e7", "e5"); // Black pawn

		// 2. Knights
		move(game, "g1", "f3"); // White knight
		move(game, "g8", "f6"); // Black knight

		// 3. Bishops
		move(game, "f1", "b5"); // White bishop
		move(game, "f8", "c5"); // Black bishop

		// Move the other black knight so it can be captured
		move(game, "b1", "c3");
		move(game, "b8", "c6");

		// 4. Capture
		move(game, "b5", "c6"); // White bishop captures black knight
		move(game, "d7", "c6"); // Black pawn captures that bishop

		// 5. Queens
		move(game, "d1", "e2"); // White queen
		move(game, "d8", "e7"); // Black queen

		// 6. Rooks
		// g1 and g8 are free because the knights moved earlier.
		move(game, "h1", "g1"); // White rook
		move(game, "h8", "g8"); // Black rook

		// 7. Kings
		// f1 and f8 are free because the bishops moved earlier.
		move(game, "e1", "f1"); // White king
		move(game, "e8", "f8"); // Black king

		System.out.println("Movement test completed.");
	}

	private static void move(Game game, String from, String to) {
		System.out.println(from + " -> " + to);
		game.move(new Tile(from), new Tile(to));
		game.debug();
	}
}