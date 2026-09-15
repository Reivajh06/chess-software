
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reivajh06.model.*;
import reivajh06.model.pieces.Bishop;
import reivajh06.model.pieces.King;
import reivajh06.model.pieces.Knight;
import reivajh06.model.pieces.Pawn;
import reivajh06.model.pieces.Piece;
import reivajh06.model.pieces.PieceColor;
import reivajh06.model.pieces.Queen;
import reivajh06.model.pieces.Rook;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Movement/navigation tests built against the current model classes.
 *
 * Production code is not modified. Game owns its Board privately, so reflection
 * is used only by the test to inspect the exact Board instance modified by
 * Game.move(...).
 *
 * The suite verifies:
 *  - all pieces in the initial position;
 *  - all 32 pieces after a simulated sequence of Game.move(...) calls;
 *  - sliding-piece blocking and friendly-piece restrictions;
 *  - pawn forward movement and pawn diagonal capture validity;
 *  - pawns losing their initial two-square move after they have moved;
 *  - valid diagonal pawn captures through Game.move(...);
 *  - invalid move selections returning false and leaving the board unchanged.
 *
 * The current Game.move(...) implementation does not automatically alternate
 * colorTurn. The play(...) helper therefore selects the moving piece's color
 * before each legal move so both colors can be exercised through Game.move(...).
 */
class GameNavigationPossiblePieceMovesTest {

    // ======================================================================
    // INITIAL POSITION
    // ======================================================================

    @ParameterizedTest(name = "Initial: {1} {2} at {0}")
    @MethodSource("startingPositionExpectations")
    void possiblePieceMovesAtStartingPosition(
            Tile from,
            PieceColor expectedColor,
            Class<? extends Piece> expectedType,
            Set<Tile> expectedMoves
    ) {
        Game game = new Game(Gamemode.PVP, Difficulty.NOT_CPU);
        Board board = extractBoard(game);

        Piece piece = board.pieceAt(from);

        assertNotNull(piece, "No piece found at starting tile " + from);
        assertEquals(expectedColor, piece.color(), "Unexpected color at " + from);
        assertInstanceOf(expectedType, piece, "Unexpected piece type at " + from);

        assertPossibleMoves(board, from, piece, expectedMoves);
    }

    static Stream<Arguments> startingPositionExpectations() {
        return Stream.of(
                // WHITE back rank
                expectation("a1", PieceColor.WHITE, Rook.class),
                expectation("b1", PieceColor.WHITE, Knight.class, "a3", "c3"),
                expectation("c1", PieceColor.WHITE, Bishop.class),
                expectation("d1", PieceColor.WHITE, Queen.class),
                expectation("e1", PieceColor.WHITE, King.class),
                expectation("f1", PieceColor.WHITE, Bishop.class),
                expectation("g1", PieceColor.WHITE, Knight.class, "f3", "h3"),
                expectation("h1", PieceColor.WHITE, Rook.class),

                // WHITE pawns: diagonals are empty, so only forward moves exist.
                expectation("a2", PieceColor.WHITE, Pawn.class, "a3", "a4"),
                expectation("b2", PieceColor.WHITE, Pawn.class, "b3", "b4"),
                expectation("c2", PieceColor.WHITE, Pawn.class, "c3", "c4"),
                expectation("d2", PieceColor.WHITE, Pawn.class, "d3", "d4"),
                expectation("e2", PieceColor.WHITE, Pawn.class, "e3", "e4"),
                expectation("f2", PieceColor.WHITE, Pawn.class, "f3", "f4"),
                expectation("g2", PieceColor.WHITE, Pawn.class, "g3", "g4"),
                expectation("h2", PieceColor.WHITE, Pawn.class, "h3", "h4"),

                // BLACK pawns
                expectation("a7", PieceColor.BLACK, Pawn.class, "a6", "a5"),
                expectation("b7", PieceColor.BLACK, Pawn.class, "b6", "b5"),
                expectation("c7", PieceColor.BLACK, Pawn.class, "c6", "c5"),
                expectation("d7", PieceColor.BLACK, Pawn.class, "d6", "d5"),
                expectation("e7", PieceColor.BLACK, Pawn.class, "e6", "e5"),
                expectation("f7", PieceColor.BLACK, Pawn.class, "f6", "f5"),
                expectation("g7", PieceColor.BLACK, Pawn.class, "g6", "g5"),
                expectation("h7", PieceColor.BLACK, Pawn.class, "h6", "h5"),

                // BLACK back rank
                expectation("a8", PieceColor.BLACK, Rook.class),
                expectation("b8", PieceColor.BLACK, Knight.class, "a6", "c6"),
                expectation("c8", PieceColor.BLACK, Bishop.class),
                expectation("d8", PieceColor.BLACK, Queen.class),
                expectation("e8", PieceColor.BLACK, King.class),
                expectation("f8", PieceColor.BLACK, Bishop.class),
                expectation("g8", PieceColor.BLACK, Knight.class, "f6", "h6"),
                expectation("h8", PieceColor.BLACK, Rook.class)
        );
    }

    // ======================================================================
    // SIMULATED MATCH POSITION
    // ======================================================================

    @ParameterizedTest(name = "Navigated: {1} {2} at {0}")
    @MethodSource("navigatedPositionExpectations")
    void possiblePieceMovesAfterGameNavigation(
            Tile from,
            PieceColor expectedColor,
            Class<? extends Piece> expectedType,
            Set<Tile> expectedMoves
    ) {
        GameContext context = createSimulatedPosition();
        Board board = context.board();

        Piece piece = board.pieceAt(from);

        assertNotNull(piece, "No piece found at navigated tile " + from);
        assertEquals(expectedColor, piece.color(), "Unexpected color at " + from);
        assertInstanceOf(expectedType, piece, "Unexpected piece type at " + from);


        assertPossibleMoves(board, from, piece, expectedMoves);
    }

    /**
     * Final simulated position:
     *
     * 8  BR .. .. BQ BK BB .. ..
     * 7  .. BP BP .. .. BP BP ..
     * 6  BP .. BN BP BB BN .. BR
     * 5  .. WB .. .. BP .. .. BP
     * 4  WP .. .. WP .. WB .. ..
     * 3  WR .. WN WQ WP .. .. ..
     * 2  .. WP WP .. .. WP WP WP
     * 1  .. .. .. .. WK .. WN WR
     *
     *    A  B  C  D  E  F  G  H
     */
    private static GameContext createSimulatedPosition() {
        Game game = new Game(Gamemode.PVP, Difficulty.NOT_CPU);
        Board board = extractBoard(game);

        play(game, board, "a2", "a4");
        play(game, board, "h7", "h5");

        play(game, board, "a1", "a3");
        play(game, board, "h8", "h6");

        play(game, board, "b1", "c3");
        play(game, board, "g8", "f6");

        play(game, board, "d2", "d4");
        play(game, board, "e7", "e5");

        play(game, board, "c1", "f4");
        play(game, board, "b8", "c6");

        play(game, board, "e2", "e3");
        play(game, board, "d7", "d6");

        play(game, board, "f1", "b5");
        play(game, board, "c8", "e6");

        play(game, board, "d1", "d3");
        play(game, board, "a7", "a6");

        return new GameContext(game, board);
    }

    static Stream<Arguments> navigatedPositionExpectations() {
        return Stream.of(
                // WHITE
                expectation("e1", PieceColor.WHITE, King.class,
                        "d1", "d2", "e2", "f1"),
                expectation("g1", PieceColor.WHITE, Knight.class,
                        "e2", "f3", "h3"),
                expectation("h1", PieceColor.WHITE, Rook.class),

                expectation("b2", PieceColor.WHITE, Pawn.class,
                        "b3", "b4"),
                expectation("c2", PieceColor.WHITE, Pawn.class),
                expectation("f2", PieceColor.WHITE, Pawn.class,
                        "f3"),
                expectation("g2", PieceColor.WHITE, Pawn.class,
                        "g3", "g4"),
                expectation("h2", PieceColor.WHITE, Pawn.class,
                        "h3", "h4"),

                expectation("a3", PieceColor.WHITE, Rook.class,
                        "a1", "a2", "b3"),
                expectation("c3", PieceColor.WHITE, Knight.class,
                        "a2", "b1", "d1", "d5", "e2", "e4"),
                expectation("d3", PieceColor.WHITE, Queen.class,
                        "c4", "d1", "d2", "e2", "e4", "f1", "f5", "g6", "h7"),
                expectation("e3", PieceColor.WHITE, Pawn.class,
                        "e4"),

                expectation("a4", PieceColor.WHITE, Pawn.class,
                        "a5"),
                expectation("d4", PieceColor.WHITE, Pawn.class,
                        "d5", "e5"),
                expectation("f4", PieceColor.WHITE, Bishop.class,
                        "e5", "g3", "g5", "h6"),
                expectation("b5", PieceColor.WHITE, Bishop.class,
                        "a6", "c4", "c6"),

                // BLACK
                expectation("e5", PieceColor.BLACK, Pawn.class,
                        "d4", "e4", "f4"),
                expectation("h5", PieceColor.BLACK, Pawn.class,
                        "h4"),

                expectation("a6", PieceColor.BLACK, Pawn.class,
                        "a5", "b5"),
                expectation("c6", PieceColor.BLACK, Knight.class,
                        "a5", "a7", "b4", "b8", "d4", "e7"),
                expectation("d6", PieceColor.BLACK, Pawn.class,
                        "d5"),
                expectation("e6", PieceColor.BLACK, Bishop.class,
                        "a2", "b3", "c4", "c8", "d5", "d7", "f5", "g4", "h3"),
                expectation("f6", PieceColor.BLACK, Knight.class,
                        "d5", "d7", "e4", "g4", "g8", "h7"),
                expectation("h6", PieceColor.BLACK, Rook.class,
                        "g6", "h7", "h8"),

                expectation("b7", PieceColor.BLACK, Pawn.class,
                        "b6"),
                expectation("c7", PieceColor.BLACK, Pawn.class),
                expectation("f7", PieceColor.BLACK, Pawn.class),
                expectation("g7", PieceColor.BLACK, Pawn.class,
                        "g5", "g6"),

                expectation("a8", PieceColor.BLACK, Rook.class,
                        "a7", "b8", "c8"),
                expectation("d8", PieceColor.BLACK, Queen.class,
                        "b8", "c8", "d7", "e7"),
                expectation("e8", PieceColor.BLACK, King.class,
                        "d7", "e7"),
                expectation("f8", PieceColor.BLACK, Bishop.class,
                        "e7")
        );
    }

    // ======================================================================
    // PAWN-SPECIFIC MOVE GENERATION
    // ======================================================================

    @Test
    void whitePawnIncludesEnemyDiagonalButNotEmptyDiagonal() {
        GameContext context = createSimulatedPosition();
        Board board = context.board();

        // D4 white pawn:
        // C5 = empty       -> excluded
        // D5 = empty ahead -> included
        // E5 = BLACK pawn  -> included as capture
        assertExpectedTiles(board, new Tile("d4"), tiles("d5", "e5"));
    }

    @Test
    void blackPawnIncludesEnemyDiagonalsButNotEmptyOnes() {
        GameContext context = createSimulatedPosition();
        Board board = context.board();

        // E5 black pawn:
        // D4 = WHITE pawn   -> capture
        // E4 = empty ahead  -> forward move
        // F4 = WHITE bishop -> capture
        assertExpectedTiles(board, new Tile("e5"), tiles("d4", "e4", "f4"));
    }

    @Test
    void pawnDoesNotIncludeFriendlyOccupiedDiagonals() {
        GameContext context = createSimulatedPosition();
        Board board = context.board();

        // B2 white pawn has WHITE rook A3 and WHITE knight C3 on its diagonals.
        assertExpectedTiles(board, new Tile("b2"), tiles("b3", "b4"));
    }

    @Test
    void pawnLosesTwoSquareAdvanceAfterLeavingStartingPosition() {
        Game game = new Game(Gamemode.PVP, Difficulty.NOT_CPU);
        Board board = extractBoard(game);

        play(game, board, "e2", "e3");

        Piece pawn = board.pieceAt(new Tile("e3"));
        assertInstanceOf(Pawn.class, pawn);

        // Once the pawn has moved, E5 must no longer be available as a second step.
        assertPossibleMoves(board, new Tile("e3"), pawn, tiles("e4"));
    }

    // ======================================================================
    // VALID PAWN CAPTURES THROUGH GAME.MOVE(...)
    // ======================================================================

    @Test
    void gameAllowsWhitePawnToCaptureEnemyDiagonally() {
        Game game = new Game(Gamemode.PVP, Difficulty.NOT_CPU);
        Board board = extractBoard(game);

        play(game, board, "e2", "e4");
        play(game, board, "d7", "d5");

        Piece whitePawn = board.pieceAt(new Tile("e4"));
        Piece blackPawn = board.pieceAt(new Tile("d5"));

        game.colorTurn = PieceColor.WHITE;
        boolean moved = game.move(new Tile("e4"), new Tile("d5"));

        printMoveResult(
                "White pawn captures enemy diagonally",
                new Tile("e4"),
                new Tile("d5"),
                true,
                moved,
                board
        );

        assertAll(
                () -> assertTrue(moved, "E4 -> D5 must be a valid white pawn capture"),
                () -> assertNull(board.pieceAt(new Tile("e4")), "E4 must be empty after capture"),
                () -> assertSame(whitePawn, board.pieceAt(new Tile("d5")), "White pawn must finish on D5"),
                () -> assertTrue(board.pieceAt(new Tile("d5")) != blackPawn,
                        "Captured black pawn must no longer occupy D5")
        );
    }

    @Test
    void gameAllowsBlackPawnToCaptureEnemyDiagonally() {
        Game game = new Game(Gamemode.PVP, Difficulty.NOT_CPU);
        Board board = extractBoard(game);

        play(game, board, "e2", "e4");
        play(game, board, "d7", "d5");
        play(game, board, "g1", "f3");

        Piece blackPawn = board.pieceAt(new Tile("d5"));
        Piece whitePawn = board.pieceAt(new Tile("e4"));

        game.colorTurn = PieceColor.BLACK;
        boolean moved = game.move(new Tile("d5"), new Tile("e4"));

        printMoveResult(
                "Black pawn captures enemy diagonally",
                new Tile("d5"),
                new Tile("e4"),
                true,
                moved,
                board
        );

        assertAll(
                () -> assertTrue(moved, "D5 -> E4 must be a valid black pawn capture"),
                () -> assertNull(board.pieceAt(new Tile("d5")), "D5 must be empty after capture"),
                () -> assertSame(blackPawn, board.pieceAt(new Tile("e4")), "Black pawn must finish on E4"),
                () -> assertTrue(board.pieceAt(new Tile("e4")) != whitePawn,
                        "Captured white pawn must no longer occupy E4")
        );
    }

    // ======================================================================
    // INVALID GAME.MOVE(...) SELECTIONS
    // ======================================================================

    @ParameterizedTest(name = "Invalid move: {0}")
    @MethodSource("invalidMoveCases")
    void invalidMoveSelectionReturnsFalseAndDoesNotChangeBoard(InvalidMoveCase testCase) {
        Game game = new Game(Gamemode.PVP, Difficulty.NOT_CPU);
        Board board = extractBoard(game);

        for (MoveStep step : testCase.setup()) {
            play(game, board, step.from(), step.to());
        }

        Tile from = new Tile(testCase.from());
        Tile to = new Tile(testCase.to());

        Piece originBefore = board.pieceAt(from);
        Piece targetBefore = board.pieceAt(to);

        assertNotNull(originBefore,
                "Invalid-move test setup must contain a piece at " + from + ": " + testCase.description());

        game.colorTurn = testCase.turn();
        PieceColor turnBefore = game.colorTurn;

        boolean moved = game.move(from, to);

        printInvalidMoveResult(
                testCase.description(),
                from,
                to,
                moved,
                turnBefore,
                game.colorTurn,
                originBefore,
                board.pieceAt(from),
                targetBefore,
                board.pieceAt(to)
        );

        assertAll(
                () -> assertFalse(moved,
                        testCase.description() + " must return false"),
                () -> assertSame(originBefore, board.pieceAt(from),
                        "Rejected move changed the origin tile " + from),
                () -> assertSame(targetBefore, board.pieceAt(to),
                        "Rejected move changed the target tile " + to),
                () -> assertEquals(turnBefore, game.colorTurn,
                        "Rejected move must not change colorTurn")
        );
    }

    static Stream<Arguments> invalidMoveCases() {
        return Stream.of(
                invalid(
                        "Pawn selects an unreachable diagonal: C2 -> D6",
                        PieceColor.WHITE,
                        "c2", "d6"
                ),
                invalid(
                        "Pawn attempts three-square forward move: C2 -> C5",
                        PieceColor.WHITE,
                        "c2", "c5"
                ),
                invalid(
                        "Pawn attempts empty diagonal capture: C2 -> D3",
                        PieceColor.WHITE,
                        "c2", "d3"
                ),
                invalid(
                        "Pawn attempts horizontal move: C2 -> B2",
                        PieceColor.WHITE,
                        "c2", "b2"
                ),
                invalid(
                        "Rook attempts to jump over its own pawn: A1 -> A3",
                        PieceColor.WHITE,
                        "a1", "a3"
                ),
                invalid(
                        "Bishop attempts to jump through its own pawn: C1 -> H6",
                        PieceColor.WHITE,
                        "c1", "h6"
                ),
                invalid(
                        "Queen attempts to jump through its own pawn: D1 -> D4",
                        PieceColor.WHITE,
                        "d1", "d4"
                ),
                invalid(
                        "Knight attempts non-L-shaped move: B1 -> B4",
                        PieceColor.WHITE,
                        "b1", "b4"
                ),
                invalid(
                        "King attempts two-square move: E1 -> E3",
                        PieceColor.WHITE,
                        "e1", "e3"
                ),
                invalid(
                        "Queen attempts to move onto friendly pawn: D1 -> D2",
                        PieceColor.WHITE,
                        "d1", "d2"
                ),
                invalid(
                        "White piece selected during BLACK turn",
                        PieceColor.BLACK,
                        "c2", "c3"
                ),
                invalid(
                        "Moved white pawn attempts to move backwards: D4 -> D3",
                        PieceColor.WHITE,
                        "d4", "d3",
                        step("d2", "d4")
                ),
                invalid(
                        "Moved rook attempts diagonal move: A3 -> B4",
                        PieceColor.WHITE,
                        "a3", "b4",
                        step("a2", "a4"),
                        step("a1", "a3")
                ),
                invalid(
                        "Moved knight attempts one-square move: C3 -> C4",
                        PieceColor.WHITE,
                        "c3", "c4",
                        step("b1", "c3")
                ),
                invalid(
                        "Moved queen attempts to cross friendly pawn: D3 -> D6",
                        PieceColor.WHITE,
                        "d3", "d6",
                        step("d2", "d4"),
                        step("d1", "d3")
                ),
                invalid(
                        "White pawn attempts to capture enemy directly forward: E4 -> E5",
                        PieceColor.WHITE,
                        "e4", "e5",
                        step("e2", "e4"),
                        step("e7", "e5")
                )
        );
    }

    // ======================================================================
    // HELPERS
    // ======================================================================

    private static void play(Game game, Board board, String fromName, String toName) {
        Tile from = new Tile(fromName);
        Tile to = new Tile(toName);
        Piece piece = board.pieceAt(from);

        assertNotNull(piece, "Simulation expected a piece at " + from);

        // Current Game.move(...) checks colorTurn but does not alternate it.
        // Select the moving color so the suite can exercise both sides.
        game.colorTurn = piece.color();

        boolean moved = game.move(from, to);

        if (!moved) {
            fail(
                    "Simulation setup move was rejected: "
                            + describePiece(piece)
                            + " " + from + " -> " + to
            );
        }
    }

    private static void assertPossibleMoves(
            Board board,
            Tile from,
            Piece piece,
            Set<Tile> expectedMoves
    ) {
        List<Tile> calculatedList = board.possiblePieceMoves(from);
        Set<Tile> calculatedMoves = new TreeSet<>(TILE_COMPARATOR);
        calculatedMoves.addAll(calculatedList);

        Set<Tile> expectedSorted = new TreeSet<>(TILE_COMPARATOR);
        expectedSorted.addAll(expectedMoves);

        Set<Tile> correctMoves = new TreeSet<>(TILE_COMPARATOR);
        correctMoves.addAll(calculatedMoves);
        correctMoves.retainAll(expectedSorted);

        Set<Tile> incorrectMoves = new TreeSet<>(TILE_COMPARATOR);
        incorrectMoves.addAll(calculatedMoves);
        incorrectMoves.removeAll(expectedSorted);

        Set<Tile> missingMoves = new TreeSet<>(TILE_COMPARATOR);
        missingMoves.addAll(expectedSorted);
        missingMoves.removeAll(calculatedMoves);

        System.out.println();
        System.out.println(
                "==== " + describePiece(piece)
                        + " at Tile " + from.name() + " ===="
        );
        System.out.println("Calculated moves: " + formatTiles(calculatedMoves));
        System.out.println("Expected moves:   " + formatTiles(expectedSorted));
        System.out.println("Correct moves:    " + formatTiles(correctMoves));
        System.out.println("Incorrect moves:  " + formatTiles(incorrectMoves));
        System.out.println("Missing moves:    " + formatTiles(missingMoves));
        System.out.println("========================================");

        assertAll(
                () -> assertEquals(
                        expectedSorted,
                        calculatedMoves,
                        "Incorrect possible moves for " + describePiece(piece) + " at " + from
                ),
                () -> assertEquals(
                        calculatedMoves.size(),
                        calculatedList.size(),
                        "Duplicate moves returned for " + describePiece(piece) + " at " + from
                )
        );
    }

    private static void assertExpectedTiles(Board board, Tile from, Set<Tile> expectedMoves) {
        Piece piece = board.pieceAt(from);
        assertNotNull(piece, "Expected a piece at " + from);
        assertPossibleMoves(board, from, piece, expectedMoves);
    }

    private static Arguments expectation(
            String from,
            PieceColor color,
            Class<? extends Piece> type,
            String... expectedMoves
    ) {
        return Arguments.of(new Tile(from), color, type, tiles(expectedMoves));
    }

    private static Set<Tile> tiles(String... names) {
        Set<Tile> result = new TreeSet<>(TILE_COMPARATOR);
        for (String name : names) {
            result.add(new Tile(name));
        }
        return result;
    }

    private static Arguments invalid(
            String description,
            PieceColor turn,
            String from,
            String to,
            MoveStep... setup
    ) {
        return Arguments.of(
                new InvalidMoveCase(
                        description,
                        turn,
                        from,
                        to,
                        List.of(setup)
                )
        );
    }

    private static MoveStep step(String from, String to) {
        return new MoveStep(from, to);
    }

    private static Board extractBoard(Game game) {
        try {
            Field boardField = Game.class.getDeclaredField("board");
            boardField.setAccessible(true);
            return (Board) boardField.get(game);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to access Game.board for testing", exception);
        }
    }

    private static String describePiece(Piece piece) {
        String color = piece.color().name().toLowerCase(Locale.ROOT);
        color = Character.toUpperCase(color.charAt(0)) + color.substring(1);
        return color + " " + piece.getClass().getSimpleName();
    }

    private static String formatTiles(Set<Tile> tiles) {
        return tiles.stream()
                .sorted(TILE_COMPARATOR)
                .map(Tile::name)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static void printMoveResult(
            String description,
            Tile from,
            Tile to,
            boolean expected,
            boolean calculated,
            Board board
    ) {
        System.out.println();
        System.out.println("==== Game.move(...) ====");
        System.out.println("Case:              " + description);
        System.out.println("Move:              " + from + " -> " + to);
        System.out.println("Expected result:   " + expected);
        System.out.println("Calculated result: " + calculated);
        System.out.println("Piece at origin:   " + pieceText(board.pieceAt(from)));
        System.out.println("Piece at target:   " + pieceText(board.pieceAt(to)));
        System.out.println("========================================");
    }

    private static void printInvalidMoveResult(
            String description,
            Tile from,
            Tile to,
            boolean calculated,
            PieceColor turnBefore,
            PieceColor turnAfter,
            Piece originBefore,
            Piece originAfter,
            Piece targetBefore,
            Piece targetAfter
    ) {
        System.out.println();
        System.out.println("==== Invalid move selection ====");
        System.out.println("Case:              " + description);
        System.out.println("Piece:             " + describePiece(originBefore));
        System.out.println("Move:              " + from + " -> " + to);
        System.out.println("Expected result:   false");
        System.out.println("Calculated result: " + calculated);
        System.out.println("Turn before:       " + turnBefore);
        System.out.println("Turn after:        " + turnAfter);
        System.out.println("Origin before:     " + pieceText(originBefore));
        System.out.println("Origin after:      " + pieceText(originAfter));
        System.out.println("Target before:     " + pieceText(targetBefore));
        System.out.println("Target after:      " + pieceText(targetAfter));
        System.out.println("========================================");
    }

    private static String pieceText(Piece piece) {
        return piece == null ? "empty" : describePiece(piece) + " (" + piece + ")";
    }

    private static final Comparator<Tile> TILE_COMPARATOR =
            Comparator.comparing((Tile tile) -> tile.col().code)
                    .thenComparingInt(Tile::row);

    private record GameContext(Game game, Board board) {
    }

    private record MoveStep(String from, String to) {
    }

    private record InvalidMoveCase(
            String description,
            PieceColor turn,
            String from,
            String to,
            List<MoveStep> setup
    ) {
    }
}
