import reivajh06.game_logic.Difficulty;
import reivajh06.game_logic.GameSession;
import reivajh06.game_logic.Gamemode;
import reivajh06.model.Tile;

import java.util.*;

public class GameTest {

	private static int testsPassed = 0;
	private static int testsFailed = 0;

	public static void main(String[] args) {

		System.out.println("============================================");
		System.out.println("          CHESS GAME SIMULATION TEST");
		System.out.println("============================================");

		testPawnStateAndEnPassant();
		testEnPassantExpiration();

		System.out.println();
		System.out.println("============================================");
		System.out.println("                FINAL RESULTS");
		System.out.println("============================================");
		System.out.println("PASSED: " + testsPassed);
		System.out.println("FAILED: " + testsFailed);

		if (testsFailed == 0) {
			System.out.println("\nALL GAME STATE TESTS PASSED");
		} else {
			System.out.println("\nSOME GAME RULES ARE NOT PERSISTING CORRECTLY");
		}
	}

	/**
	 * Tests:
	 *
	 * - Pawn can initially move one or two squares.
	 * - Pawn loses its double-move after moving.
	 * - Pawn state is independent for every pawn.
	 * - En passant becomes available after a double pawn move.
	 * - En passant moves the capturing pawn correctly.
	 * - En passant removes the captured pawn.
	 */
	private static void testPawnStateAndEnPassant() {

		System.out.println();
		System.out.println("============================================");
		System.out.println(" TEST 1 - PAWN STATE + EN PASSANT");
		System.out.println("============================================");

		GameSession game =
				new GameSession(Gamemode.PVAI, Difficulty.NOT_CPU);

		game.setDebug(true);

		game.printBoard();

		/*
		 * Initial pawn state.
		 *
		 * E2 should be able to move:
		 *
		 * E3
		 * E4
		 */
		expectExactMoves(
				game,
				"E2",
				"E3", "E4"
		);

		/*
		 * WHITE
		 *
		 * E2 -> E4
		 */
		play(game, "E2", "E4");

		/*
		 * BLACK
		 *
		 * A7 has never moved, therefore its
		 * double starting move must still exist.
		 */
		expectExactMoves(
				game,
				"A7",
				"A6", "A5"
		);

		play(game, "A7", "A6");

		/*
		 * WHITE
		 *
		 * E4 pawn has already moved.
		 *
		 * E6 MUST NOT be available anymore.
		 */
		expectExactMoves(
				game,
				"E4",
				"E5"
		);

		expectMoveMissing(
				game,
				"E4",
				"E6",
				"Pawn must not move two squares after leaving its starting tile"
		);

		play(game, "E4", "E5");

		/*
		 * BLACK
		 *
		 * A7 -> A6 happened previously.
		 *
		 * Therefore A6 -> A4 must NOT be possible.
		 */
		expectExactMoves(
				game,
				"A6",
				"A5"
		);

		expectMoveMissing(
				game,
				"A6",
				"A4",
				"Black pawn must also lose its starting double move"
		);

		/*
		 * D7 has NOT moved.
		 *
		 * Starting double move should still exist independently
		 * of the state of the A pawn.
		 */
		expectExactMoves(
				game,
				"D7",
				"D6", "D5"
		);

		/*
		 * BLACK
		 *
		 * D7 -> D5 creates an en passant opportunity
		 * for the pawn at E5.
		 */
		play(game, "D7", "D5");

		/*
		 * WHITE
		 *
		 * E5 should now be able to:
		 *
		 * E6 - normal move
		 * D6 - en passant
		 */
		expectExactMoves(
				game,
				"E5",
				"E6", "D6"
		);

		expectMoveAvailable(
				game,
				"E5",
				"D6",
				"En passant should exist immediately after D7 -> D5"
		);

		/*
		 * Execute en passant.
		 */
		play(game, "E5", "D6");

		/*
		 * BLACK TURN
		 *
		 * If en passant worked properly, the pawn that was
		 * located at D5 must have been removed.
		 *
		 * If it still existed, it would normally be able
		 * to move D5 -> D4.
		 */
		expectNoMoves(
				game,
				"D5",
				"Pawn captured by en passant must be removed from D5"
		);

		/*
		 * Make a normal Black move so White gets the turn again.
		 */
		play(game, "G8", "F6");

		/*
		 * WHITE
		 *
		 * Pawn is now at D6.
		 *
		 * Possible moves:
		 *
		 * D7 normal
		 * C7 capture
		 * E7 capture
		 */
		expectExactMoves(
				game,
				"D6",
				"D7", "C7", "E7"
		);

		/*
		 * It originated at E2 and has moved several times.
		 *
		 * It must NOT somehow regain its double move.
		 */
		expectMoveMissing(
				game,
				"D6",
				"D8",
				"Moved pawn must never regain starting double movement"
		);

		/*
		 * Another pawn that has never moved must still
		 * have its starting double move.
		 */
		expectExactMoves(
				game,
				"A2",
				"A3", "A4"
		);

		System.out.println();
		System.out.println("TEST 1 FINISHED");
	}

	/**
	 * En passant is only valid immediately after the opponent
	 * performs the double pawn move.
	 *
	 * If another move happens first, the opportunity disappears.
	 */
	private static void testEnPassantExpiration() {

		System.out.println();
		System.out.println("============================================");
		System.out.println(" TEST 2 - EN PASSANT EXPIRATION");
		System.out.println("============================================");

		GameSession game =
				new GameSession(Gamemode.PVAI, Difficulty.NOT_CPU);

		game.setDebug(true);

		/*
		 * Build the en passant situation.
		 */

		play(game, "E2", "E4");
		play(game, "A7", "A6");

		play(game, "E4", "E5");
		play(game, "D7", "D5");

		/*
		 * At this exact moment en passant MUST exist.
		 */
		expectMoveAvailable(
				game,
				"E5",
				"D6",
				"En passant should initially be available"
		);

		/*
		 * White decides NOT to perform en passant.
		 */
		play(game, "G1", "F3");

		/*
		 * Black makes another move.
		 */
		play(game, "A6", "A5");

		/*
		 * WHITE TURN AGAIN
		 *
		 * En passant must now have expired.
		 */
		expectMoveMissing(
				game,
				"E5",
				"D6",
				"En passant must disappear after one turn"
		);

		/*
		 * Only E6 should remain.
		 */
		expectExactMoves(
				game,
				"E5",
				"E6"
		);

		System.out.println();
		System.out.println("TEST 2 FINISHED");
	}

	// ============================================================
	// MOVE SIMULATION
	// ============================================================

	private static void play(
			GameSession game,
			String from,
			String to) {

		System.out.println();
		System.out.println("--------------------------------------------");
		System.out.println("TURN: " + game.colorTurn());
		System.out.println("MOVE: " + from + " -> " + to);

		Set<String> available =
				getMoves(game, from);

		System.out.println(
				"Available from " + from + ": " + sorted(available)
		);

		if (!available.contains(to)) {

			fail(
					"Attempted simulation move "
							+ from + " -> " + to
							+ " but it was not returned by pieceMovesetTiles()"
			);

			return;
		}

		game.move(
				new Tile(from),
				new Tile(to)
		);

		pass(
				"Move executed: "
						+ from + " -> " + to
		);

		game.printBoard();
	}

	// ============================================================
	// RULE ASSERTIONS
	// ============================================================

	private static void expectExactMoves(
			GameSession game,
			String location,
			String... expectedValues) {

		Set<String> actual =
				getMoves(game, location);

		Set<String> expected =
				set(expectedValues);

		Set<String> incorrect =
				new HashSet<>(actual);

		incorrect.removeAll(expected);

		Set<String> missing =
				new HashSet<>(expected);

		missing.removeAll(actual);

		System.out.println();
		System.out.println("CHECKING " + location);
		System.out.println("Expected : " + sorted(expected));
		System.out.println("Actual   : " + sorted(actual));

		if (incorrect.isEmpty() && missing.isEmpty()) {

			pass(
					location + " moveset is correct"
			);

		} else {

			System.out.println(
					"Incorrect: " + sorted(incorrect)
			);

			System.out.println(
					"Missing  : " + sorted(missing)
			);

			fail(
					location + " returned an incorrect moveset"
			);
		}
	}

	private static void expectMoveAvailable(
			GameSession game,
			String from,
			String expectedMove,
			String description) {

		Set<String> actual =
				getMoves(game, from);

		System.out.println();
		System.out.println("RULE CHECK: " + description);
		System.out.println(from + " moves: " + sorted(actual));

		if (actual.contains(expectedMove)) {

			pass(
					expectedMove + " is correctly available"
			);

		} else {

			fail(
					expectedMove
							+ " should be available from "
							+ from
			);
		}
	}

	private static void expectMoveMissing(
			GameSession game,
			String from,
			String forbiddenMove,
			String description) {

		Set<String> actual =
				getMoves(game, from);

		System.out.println();
		System.out.println("RULE CHECK: " + description);
		System.out.println(from + " moves: " + sorted(actual));

		if (!actual.contains(forbiddenMove)) {

			pass(
					forbiddenMove + " is correctly unavailable"
			);

		} else {

			fail(
					forbiddenMove
							+ " should NOT be available from "
							+ from
			);
		}
	}

	private static void expectNoMoves(
			GameSession game,
			String location,
			String description) {

		Set<String> actual =
				getMoves(game, location);

		System.out.println();
		System.out.println("RULE CHECK: " + description);
		System.out.println(location + " moves: " + sorted(actual));

		if (actual.isEmpty()) {

			pass(
					location + " correctly contains no movable piece"
			);

		} else {

			fail(
					location
							+ " unexpectedly returned moves "
							+ sorted(actual)
			);
		}
	}

	// ============================================================
	// TILE UTILITIES
	// ============================================================

	private static Set<String> getMoves(
			GameSession game,
			String location) {

		List<Tile> tiles =
				game.pieceMovesetTiles(
						new Tile(location)
				);

		Set<String> names =
				new HashSet<>();

		if (tiles == null) {
			return names;
		}

		for (Tile tile : tiles) {

			names.add(
					tile.toString().toUpperCase()
			);
		}

		return names;
	}

	private static Set<String> set(
			String... values) {

		return new HashSet<>(
				Arrays.asList(values)
		);
	}

	private static List<String> sorted(
			Collection<String> values) {

		List<String> result =
				new ArrayList<>(values);

		Collections.sort(result);

		return result;
	}

	// ============================================================
	// TEST OUTPUT
	// ============================================================

	private static void pass(String message) {

		testsPassed++;

		System.out.println(
				"[PASS] " + message
		);
	}

	private static void fail(String message) {

		testsFailed++;

		System.out.println(
				"[FAIL] " + message
		);
	}
}