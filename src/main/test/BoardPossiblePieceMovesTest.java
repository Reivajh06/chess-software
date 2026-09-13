import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reivajh06.model.Board;
import reivajh06.model.Tile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BoardPossiblePieceMovesTest {

	private Board board;

	@BeforeEach
	void setUp() {
		board = new Board();
	}

	@ParameterizedTest(name = "{0} at {1}")
	@MethodSource("startingPieces")
	void shouldReturnPossibleMovesForEveryStartingPiece(
			String piece,
			String from,
			Set<String> expectedMoves
	) {
		List<Tile> moves = board.possiblePieceMoves(new Tile(from));

		Set<String> actualMoves = moves.stream()
				.map(BoardPossiblePieceMovesTest::toSquare)
				.collect(Collectors.toSet());

		Set<String> correctMoves = new TreeSet<>(actualMoves);
		correctMoves.retainAll(expectedMoves);

		Set<String> incorrectMoves = new TreeSet<>(actualMoves);
		incorrectMoves.removeAll(expectedMoves);

		Set<String> missingMoves = new TreeSet<>(expectedMoves);
		missingMoves.removeAll(actualMoves);

		System.out.println();
		System.out.println(
				"==== " + piece + " at Tile " + from.toUpperCase(Locale.ROOT) + " ===="
		);

		System.out.println(
				"Calculated moves: " + formatMoves(actualMoves)
		);

		System.out.println(
				"Expected moves:   " + formatMoves(expectedMoves)
		);

		System.out.println(
				"Correct moves:    " + formatMoves(correctMoves)
		);

		System.out.println(
				"Incorrect moves:  " + formatMoves(incorrectMoves)
		);

		System.out.println(
				"Missing moves:    " + formatMoves(missingMoves)
		);

		System.out.println("========================================");
		System.out.println();

		assertAll(
				() -> assertEquals(
						expectedMoves,
						actualMoves,
						"Incorrect moves for " + piece + " at " + from
				),
				() -> assertEquals(
						actualMoves.size(),
						moves.size(),
						"Duplicate moves returned for " + piece + " at " + from
				)
		);
	}

	private static String toSquare(Tile tile) {
		return tile.col().name().toLowerCase(Locale.ROOT) + tile.row();
	}

	private static String formatMoves(Set<String> moves) {
		return moves.stream()
				.sorted()
				.map(String::toUpperCase)
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private static Arguments moves(
			String piece,
			String from,
			String... destinations
	) {
		return Arguments.of(
				piece,
				from,
				Set.of(destinations)
		);
	}

	static Stream<Arguments> startingPieces() {
		return Stream.of(

				// ============================================================
				// WHITE - BACK RANK
				// ============================================================

				moves(
						"White Rook",
						"a1",
						"a2", "a3", "a4", "a5", "a6", "a7", "a8",
						"b1", "c1", "d1", "e1", "f1", "g1", "h1"
				),

				moves(
						"White Knight",
						"b1",
						"a3", "c3", "d2"
				),

				moves(
						"White Bishop",
						"c1",
						"b2", "a3",
						"d2", "e3", "f4", "g5", "h6"
				),

				moves(
						"White Queen",
						"d1",

						"d2", "d3", "d4", "d5", "d6", "d7", "d8",

						"a1", "b1", "c1",
						"e1", "f1", "g1", "h1",

						"c2", "b3", "a4",
						"e2", "f3", "g4", "h5"
				),

				moves(
						"White King",
						"e1",
						"d1", "d2",
						"e2",
						"f2", "f1"
				),

				moves(
						"White Bishop",
						"f1",
						"e2", "d3", "c4", "b5", "a6",
						"g2", "h3"
				),

				moves(
						"White Knight",
						"g1",
						"e2", "f3", "h3"
				),

				moves(
						"White Rook",
						"h1",
						"h2", "h3", "h4", "h5", "h6", "h7", "h8",
						"a1", "b1", "c1", "d1", "e1", "f1", "g1"
				),


				// ============================================================
				// WHITE PAWNS
				// ============================================================

				moves(
						"White Pawn",
						"a2",
						"a3", "a4",
						"b3"
				),

				moves(
						"White Pawn",
						"b2",
						"b3", "b4",
						"a3", "c3"
				),

				moves(
						"White Pawn",
						"c2",
						"c3", "c4",
						"b3", "d3"
				),

				moves(
						"White Pawn",
						"d2",
						"d3", "d4",
						"c3", "e3"
				),

				moves(
						"White Pawn",
						"e2",
						"e3", "e4",
						"d3", "f3"
				),

				moves(
						"White Pawn",
						"f2",
						"f3", "f4",
						"e3", "g3"
				),

				moves(
						"White Pawn",
						"g2",
						"g3", "g4",
						"f3", "h3"
				),

				moves(
						"White Pawn",
						"h2",
						"h3", "h4",
						"g3"
				),


				// ============================================================
				// BLACK - BACK RANK
				// ============================================================

				moves(
						"Black Rook",
						"a8",
						"a1", "a2", "a3", "a4", "a5", "a6", "a7",
						"b8", "c8", "d8", "e8", "f8", "g8", "h8"
				),

				moves(
						"Black Knight",
						"b8",
						"a6", "c6", "d7"
				),

				moves(
						"Black Bishop",
						"c8",
						"b7", "a6",
						"d7", "e6", "f5", "g4", "h3"
				),

				moves(
						"Black Queen",
						"d8",

						"d1", "d2", "d3", "d4", "d5", "d6", "d7",

						"a8", "b8", "c8",
						"e8", "f8", "g8", "h8",

						"c7", "b6", "a5",
						"e7", "f6", "g5", "h4"
				),

				moves(
						"Black King",
						"e8",
						"d8", "d7",
						"e7",
						"f7", "f8"
				),

				moves(
						"Black Bishop",
						"f8",
						"e7", "d6", "c5", "b4", "a3",
						"g7", "h6"
				),

				moves(
						"Black Knight",
						"g8",
						"e7", "f6", "h6"
				),

				moves(
						"Black Rook",
						"h8",
						"h1", "h2", "h3", "h4", "h5", "h6", "h7",
						"a8", "b8", "c8", "d8", "e8", "f8", "g8"
				),


				// ============================================================
				// BLACK PAWNS
				// ============================================================

				moves(
						"Black Pawn",
						"a7",
						"a6", "a5",
						"b6"
				),

				moves(
						"Black Pawn",
						"b7",
						"b6", "b5",
						"a6", "c6"
				),

				moves(
						"Black Pawn",
						"c7",
						"c6", "c5",
						"b6", "d6"
				),

				moves(
						"Black Pawn",
						"d7",
						"d6", "d5",
						"c6", "e6"
				),

				moves(
						"Black Pawn",
						"e7",
						"e6", "e5",
						"d6", "f6"
				),

				moves(
						"Black Pawn",
						"f7",
						"f6", "f5",
						"e6", "g6"
				),

				moves(
						"Black Pawn",
						"g7",
						"g6", "g5",
						"f6", "h6"
				),

				moves(
						"Black Pawn",
						"h7",
						"h6", "h5",
						"g6"
				)
		);
	}
}