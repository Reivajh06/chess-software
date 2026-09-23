import reivajh06.game_logic.Difficulty;
import reivajh06.game_logic.GameSession;
import reivajh06.game_logic.Gamemode;
import reivajh06.model.Tile;

import java.util.*;

public class PieceMovesetTest {

	public static void main(String[] args) {

		testWhitePieces();
		testBlackPieces();
	}

	private static void testWhitePieces() {

		GameSession gameSession =
				new GameSession(Gamemode.PVAI, Difficulty.NOT_CPU);

		gameSession.setDebug(true);

		Map<String, Set<String>> expectedMoves = new LinkedHashMap<>();

		expectedMoves.put("A1", set());
		expectedMoves.put("B1", set("A3", "C3"));
		expectedMoves.put("C1", set());
		expectedMoves.put("D1", set());
		expectedMoves.put("E1", set());
		expectedMoves.put("F1", set());
		expectedMoves.put("G1", set("F3", "H3"));
		expectedMoves.put("H1", set());

		expectedMoves.put("A2", set("A3", "A4"));
		expectedMoves.put("B2", set("B3", "B4"));
		expectedMoves.put("C2", set("C3", "C4"));
		expectedMoves.put("D2", set("D3", "D4"));
		expectedMoves.put("E2", set("E3", "E4"));
		expectedMoves.put("F2", set("F3", "F4"));
		expectedMoves.put("G2", set("G3", "G4"));
		expectedMoves.put("H2", set("H3", "H4"));

		System.out.println();
		System.out.println("========================================");
		System.out.println("          TESTING WHITE PIECES");
		System.out.println("========================================");
		System.out.println("Current turn: " + gameSession.colorTurn());

		testMovesets(gameSession, expectedMoves);
	}

	private static void testBlackPieces() {

		GameSession gameSession =
				new GameSession(Gamemode.PVAI, Difficulty.NOT_CPU);

		gameSession.setDebug(true);

		/*
		 * Advance the game to Black's turn.
		 *
		 * E2 -> E3 does not alter Black's initial possible moves.
		 */
		gameSession.move(
				new Tile("E2"),
				new Tile("E3")
		);

		Map<String, Set<String>> expectedMoves = new LinkedHashMap<>();

		expectedMoves.put("A7", set("A6", "A5"));
		expectedMoves.put("B7", set("B6", "B5"));
		expectedMoves.put("C7", set("C6", "C5"));
		expectedMoves.put("D7", set("D6", "D5"));
		expectedMoves.put("E7", set("E6", "E5"));
		expectedMoves.put("F7", set("F6", "F5"));
		expectedMoves.put("G7", set("G6", "G5"));
		expectedMoves.put("H7", set("H6", "H5"));

		expectedMoves.put("A8", set());
		expectedMoves.put("B8", set("A6", "C6"));
		expectedMoves.put("C8", set());
		expectedMoves.put("D8", set());
		expectedMoves.put("E8", set());
		expectedMoves.put("F8", set());
		expectedMoves.put("G8", set("F6", "H6"));
		expectedMoves.put("H8", set());

		System.out.println();
		System.out.println("========================================");
		System.out.println("          TESTING BLACK PIECES");
		System.out.println("========================================");
		System.out.println("Current turn: " + gameSession.colorTurn());

		testMovesets(gameSession, expectedMoves);
	}

	private static void testMovesets(
			GameSession gameSession,
			Map<String, Set<String>> expectedMoves) {

		int passed = 0;
		int failed = 0;

		for (Map.Entry<String, Set<String>> entry : expectedMoves.entrySet()) {

			String location = entry.getKey();
			Set<String> expected = entry.getValue();

			List<Tile> result =
					gameSession.pieceMovesetTiles(new Tile(location));

			Set<String> actual = tileNames(result);

			/*
			 * Returned by the application,
			 * but should not be legal.
			 */
			Set<String> incorrectMoves = new HashSet<>(actual);
			incorrectMoves.removeAll(expected);

			/*
			 * Expected to be legal,
			 * but not returned by the application.
			 */
			Set<String> missingMoves = new HashSet<>(expected);
			missingMoves.removeAll(actual);

			boolean correct =
					incorrectMoves.isEmpty()
							&& missingMoves.isEmpty();

			System.out.println();
			System.out.println("----------------------------------------");
			System.out.println("Piece: " + location);

			System.out.println(
					"Expected  : " + sorted(expected)
			);

			System.out.println(
					"Returned  : " + sorted(actual)
			);

			if (correct) {

				System.out.println("RESULT    : CORRECT");
				passed++;

			} else {

				System.out.println("RESULT    : INCORRECT");

				System.out.println(
						"Incorrect : " + sorted(incorrectMoves)
				);

				System.out.println(
						"Missing   : " + sorted(missingMoves)
				);

				failed++;
			}
		}

		System.out.println();
		System.out.println("========================================");
		System.out.println("SUMMARY");
		System.out.println("========================================");

		System.out.println("Passed: " + passed);
		System.out.println("Failed: " + failed);
		System.out.println("Total : " + expectedMoves.size());
	}

	private static Set<String> tileNames(List<Tile> tiles) {

		Set<String> result = new HashSet<>();

		for (Tile tile : tiles) {
			result.add(tile.toString().toUpperCase());
		}

		return result;
	}

	private static Set<String> set(String... values) {
		return new HashSet<>(Arrays.asList(values));
	}

	private static List<String> sorted(Collection<String> values) {

		List<String> result = new ArrayList<>(values);
		Collections.sort(result);

		return result;
	}
}