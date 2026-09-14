import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reivajh06.model.Board;
import reivajh06.model.Tile;
import reivajh06.model.pieces.Piece;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class BoardPossiblePieceMovesTest {

	private Board board;

	@BeforeEach
	void setUp() {
		board = new Board();
	}

	@ParameterizedTest(name = "{index} => Tile {0}")
	@MethodSource("startingPositions")
	void possiblePieceMovesFromStartingPosition(Tile from, Set<Tile> expectedMoves) {
		Piece piece = board.pieceAt(from);
		assertNotNull(piece, "No piece found at starting tile " + from);

		String pieceDescription = formatColor(piece) + " " + piece.getClass().getSimpleName();

		List<Tile> calculatedMoves;

		try {
			calculatedMoves = board.possiblePieceMoves(from);
		} catch (RuntimeException exception) {
			System.out.println();
			System.out.println("==== " + pieceDescription + " at Tile " + from + " ====");
			System.out.println("Calculated moves: <ERROR: "
					+ exception.getClass().getSimpleName()
					+ (exception.getMessage() == null ? "" : ": " + exception.getMessage())
					+ ">");
			System.out.println("Expected moves:   " + formatTiles(expectedMoves));
			System.out.println("Correct moves:    <not available>");
			System.out.println("Incorrect moves:  <not available>");
			System.out.println("Missing moves:    <not available>");
			System.out.println("========================================");
			System.out.println();

			fail("possiblePieceMoves threw " + exception.getClass().getSimpleName()
					+ " for " + pieceDescription + " at " + from, exception);
			return;
		}

		Set<Tile> actualMoves = new HashSet<>(calculatedMoves);

		Set<Tile> correctMoves = new HashSet<>(actualMoves);
		correctMoves.retainAll(expectedMoves);

		Set<Tile> incorrectMoves = new HashSet<>(actualMoves);
		incorrectMoves.removeAll(expectedMoves);

		Set<Tile> missingMoves = new HashSet<>(expectedMoves);
		missingMoves.removeAll(actualMoves);

		System.out.println();
		System.out.println("==== " + pieceDescription + " at Tile " + from + " ====");
		System.out.println("Calculated moves: " + formatTiles(actualMoves));
		System.out.println("Expected moves:   " + formatTiles(expectedMoves));
		System.out.println("Correct moves:    " + formatTiles(correctMoves));
		System.out.println("Incorrect moves:  " + formatTiles(incorrectMoves));
		System.out.println("Missing moves:    " + formatTiles(missingMoves));
		System.out.println("========================================");
		System.out.println();

		assertAll(
				() -> assertEquals(
						expectedMoves,
						actualMoves,
						"Incorrect moves for " + pieceDescription + " at " + from
				),
				() -> assertEquals(
						calculatedMoves.size(),
						actualMoves.size(),
						"Duplicate moves returned for " + pieceDescription + " at " + from
				)
		);
	}

	private static String formatColor(Piece piece) {
		String color = piece.color().name().toLowerCase(Locale.ROOT);
		return Character.toUpperCase(color.charAt(0)) + color.substring(1);
	}

	private static String formatTiles(Collection<Tile> tiles) {
		return tiles.stream()
				.sorted(
						Comparator.comparingInt((Tile tile) -> tile.col().ordinal())
								.thenComparingInt(Tile::row)
				)
				.map(Tile::toString)
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private static Set<Tile> tiles(String... names) {
		return Stream.of(names)
				.map(Tile::new)
				.collect(Collectors.toSet());
	}

	private static Arguments testCase(String from, String... expectedMoves) {
		return Arguments.of(new Tile(from), tiles(expectedMoves));
	}

	static Stream<Arguments> startingPositions() {
		return Stream.of(
				// ============================================================
				// WHITE BACK RANK
				// Same-color pieces block the destination and every tile behind it.
				// ============================================================

				testCase("a1"),
				testCase("b1", "a3", "c3"),
				testCase("c1"),
				testCase("d1"),
				testCase("e1"),
				testCase("f1"),
				testCase("g1", "f3", "h3"),
				testCase("h1"),

				// ============================================================
				// WHITE PAWNS
				// Pawn move generation in the current model includes forward,
				// forward-left and forward-right movement definitions.
				// Starting pawns have two forward repetitions.
				// ============================================================

				testCase("a2", "a3", "a4", "b3"),
				testCase("b2", "a3", "b3", "b4", "c3"),
				testCase("c2", "b3", "c3", "c4", "d3"),
				testCase("d2", "c3", "d3", "d4", "e3"),
				testCase("e2", "d3", "e3", "e4", "f3"),
				testCase("f2", "e3", "f3", "f4", "g3"),
				testCase("g2", "f3", "g3", "g4", "h3"),
				testCase("h2", "g3", "h3", "h4"),

				// ============================================================
				// BLACK BACK RANK
				// ============================================================

				testCase("a8"),
				testCase("b8", "a6", "c6"),
				testCase("c8"),
				testCase("d8"),
				testCase("e8"),
				testCase("f8"),
				testCase("g8", "f6", "h6"),
				testCase("h8"),

				// ============================================================
				// BLACK PAWNS
				// ============================================================

				testCase("a7", "a5", "a6", "b6"),
				testCase("b7", "a6", "b5", "b6", "c6"),
				testCase("c7", "b6", "c5", "c6", "d6"),
				testCase("d7", "c6", "d5", "d6", "e6"),
				testCase("e7", "d6", "e5", "e6", "f6"),
				testCase("f7", "e6", "f5", "f6", "g6"),
				testCase("g7", "f6", "g5", "g6", "h6"),
				testCase("h7", "g6", "h5", "h6")
		);
	}
}
