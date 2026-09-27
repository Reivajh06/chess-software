import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.function.Executable;
import software.ulpgc.gamelogic.Difficulty;
import software.ulpgc.gamelogic.GameSession;
import software.ulpgc.gamelogic.GameStatus;
import software.ulpgc.gamelogic.Gamemode;
import software.ulpgc.model.Board;
import software.ulpgc.model.Piece;
import software.ulpgc.model.PieceColor;
import software.ulpgc.model.PieceType;
import software.ulpgc.model.Tile;
import software.ulpgc.model.movement.Move;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * End-to-end chess simulations for the public GameSession API.
 *
 * Every test:
 *  1. prints what the simulated match is intended to verify,
 *  2. executes only moves reported as legal by GameSession,
 *  3. records every objective instead of failing immediately,
 *  4. prints accomplished and unmet objectives,
 *  5. finally reports unmet objectives to JUnit with expected vs actual details.
 *
 * The Board field is read through reflection only for verification. No board state
 * is modified directly by these tests; all moves still go through GameSession.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ChessGameSimulationTest {

    private static final Field BOARD_FIELD = boardField();

    @Test
    @DisplayName("01 - White wins with Scholar's Mate")
    void whiteWinsWithScholarsMate() {
        MatchReport report = new MatchReport(
                "WHITE WIN - Scholar's Mate",
                "Simulate Scholar's Mate and verify that the engine declares WHITE_WIN."
        );
        GameSession game = newGame();

        SequenceResult sequence = playSequence(game,
                "e2", "e4",
                "e7", "e5",
                "f1", "c4",
                "b8", "c6",
                "d1", "h5",
                "g8", "f6",
                "h5", "f7"
        );

        report.check(
                "All Scholar's Mate moves are accepted",
                sequence.success(),
                "All 7 half-moves in the supplied sequence reach the final Qxf7# position.",
                sequence.details()
        );
        report.check(
                "White is declared the winner",
                game.status() == GameStatus.WHITE_WIN,
                "GameStatus.WHITE_WIN after Qxf7#.",
                "Game status was " + game.status() + "."
        );
        report.checkPiece(
                game,
                "Final attacking queen is on F7",
                "f7",
                PieceColor.WHITE,
                PieceType.QUEEN
        );

        report.finish();
    }

    @Test
    @DisplayName("02 - Black wins with Fool's Mate")
    void blackWinsWithFoolsMate() {
        MatchReport report = new MatchReport(
                "BLACK WIN - Fool's Mate",
                "Simulate Fool's Mate and verify that the engine declares BLACK_WIN."
        );
        GameSession game = newGame();

        SequenceResult sequence = playSequence(game,
                "f2", "f3",
                "e7", "e5",
                "g2", "g4",
                "d8", "h4"
        );

        report.check(
                "All Fool's Mate moves are accepted",
                sequence.success(),
                "All 4 moves reach the final ...Qh4# position.",
                sequence.details()
        );
        report.check(
                "Black is declared the winner",
                game.status() == GameStatus.BLACK_WIN,
                "GameStatus.BLACK_WIN after ...Qh4#.",
                "Game status was " + game.status() + "."
        );
        report.checkPiece(
                game,
                "Final attacking queen is on H4",
                "h4",
                PieceColor.BLACK,
                PieceType.QUEEN
        );

        report.finish();
    }

    @Test
    @DisplayName("03 - White kingside castling")
    void whiteCanCastleKingside() {
        MatchReport report = new MatchReport(
                "WHITE KINGSIDE CASTLING",
                "Clear F1 and G1, then verify O-O: king E1->G1 and rook H1->F1."
        );
        GameSession game = newGame();

        SequenceResult setup = playSequence(game,
                "e2", "e4",
                "e7", "e5",
                "g1", "f3",
                "b8", "c6",
                "f1", "e2",
                "g8", "f6"
        );
        report.check(
                "Castling setup is reached",
                setup.success(),
                "F1 and G1 are clear while king E1 and rook H1 remain unmoved.",
                setup.details()
        );

        MoveAttempt castle = attemptMove(game, "e1", "g1");
        report.check(
                "White kingside castling move is accepted",
                castle.success(),
                "E1->G1 is a legal move in this position.",
                castle.details()
        );
        report.checkPiece(game, "White king ends on G1", "g1", PieceColor.WHITE, PieceType.KING);
        report.checkPiece(game, "White rook ends on F1", "f1", PieceColor.WHITE, PieceType.ROOK);
        report.checkEmpty(game, "E1 is empty after castling", "e1");
        report.checkEmpty(game, "H1 is empty after castling", "h1");

        report.finish();
    }

    @Test
    @DisplayName("04 - White queenside castling")
    void whiteCanCastleQueenside() {
        MatchReport report = new MatchReport(
                "WHITE QUEENSIDE CASTLING",
                "Clear B1, C1 and D1, then verify O-O-O: king E1->C1 and rook A1->D1."
        );
        GameSession game = newGame();

        SequenceResult setup = playSequence(game,
                "d2", "d4",
                "d7", "d5",
                "c1", "f4",
                "b8", "c6",
                "b1", "c3",
                "g8", "f6",
                "d1", "d2",
                "e7", "e6"
        );
        report.check(
                "Castling setup is reached",
                setup.success(),
                "B1, C1 and D1 are clear while king E1 and rook A1 remain unmoved.",
                setup.details()
        );

        MoveAttempt castle = attemptMove(game, "e1", "c1");
        report.check(
                "White queenside castling move is accepted",
                castle.success(),
                "E1->C1 is a legal move in this position.",
                castle.details()
        );
        report.checkPiece(game, "White king ends on C1", "c1", PieceColor.WHITE, PieceType.KING);
        report.checkPiece(game, "White rook ends on D1", "d1", PieceColor.WHITE, PieceType.ROOK);
        report.checkEmpty(game, "E1 is empty after castling", "e1");
        report.checkEmpty(game, "A1 is empty after castling", "a1");

        report.finish();
    }

    @Test
    @DisplayName("05 - Black kingside castling")
    void blackCanCastleKingside() {
        MatchReport report = new MatchReport(
                "BLACK KINGSIDE CASTLING",
                "Clear F8 and G8, then verify ...O-O: king E8->G8 and rook H8->F8."
        );
        GameSession game = newGame();

        SequenceResult setup = playSequence(game,
                "e2", "e4",
                "e7", "e5",
                "g1", "f3",
                "g8", "f6",
                "f1", "e2",
                "f8", "e7",
                "d2", "d3"
        );
        report.check(
                "Castling setup is reached",
                setup.success(),
                "F8 and G8 are clear while king E8 and rook H8 remain unmoved, with Black to move.",
                setup.details()
        );

        MoveAttempt castle = attemptMove(game, "e8", "g8");
        report.check(
                "Black kingside castling move is accepted",
                castle.success(),
                "E8->G8 is a legal move in this position.",
                castle.details()
        );
        report.checkPiece(game, "Black king ends on G8", "g8", PieceColor.BLACK, PieceType.KING);
        report.checkPiece(game, "Black rook ends on F8", "f8", PieceColor.BLACK, PieceType.ROOK);
        report.checkEmpty(game, "E8 is empty after castling", "e8");
        report.checkEmpty(game, "H8 is empty after castling", "h8");

        report.finish();
    }

    @Test
    @DisplayName("06 - Black queenside castling")
    void blackCanCastleQueenside() {
        MatchReport report = new MatchReport(
                "BLACK QUEENSIDE CASTLING",
                "Clear B8, C8 and D8, then verify ...O-O-O: king E8->C8 and rook A8->D8."
        );
        GameSession game = newGame();

        SequenceResult setup = playSequence(game,
                "d2", "d4",
                "d7", "d5",
                "c1", "f4",
                "c8", "f5",
                "b1", "c3",
                "b8", "c6",
                "d1", "d2",
                "d8", "d7",
                "e2", "e3"
        );
        report.check(
                "Castling setup is reached",
                setup.success(),
                "B8, C8 and D8 are clear while king E8 and rook A8 remain unmoved, with Black to move.",
                setup.details()
        );

        MoveAttempt castle = attemptMove(game, "e8", "c8");
        report.check(
                "Black queenside castling move is accepted",
                castle.success(),
                "E8->C8 is a legal move in this position.",
                castle.details()
        );
        report.checkPiece(game, "Black king ends on C8", "c8", PieceColor.BLACK, PieceType.KING);
        report.checkPiece(game, "Black rook ends on D8", "d8", PieceColor.BLACK, PieceType.ROOK);
        report.checkEmpty(game, "E8 is empty after castling", "e8");
        report.checkEmpty(game, "A8 is empty after castling", "a8");

        report.finish();
    }

    @Test
    @DisplayName("07 - Valid en passant")
    void validEnPassantCapture() {
        MatchReport report = new MatchReport(
                "EN PASSANT",
                "Create a legal white en-passant capture E5xD6 after Black has just played D7->D5."
        );
        GameSession game = newGame();

        SequenceResult setup = playSequence(game,
                "e2", "e4",
                "a7", "a6",
                "e4", "e5",
                "d7", "d5"
        );
        report.check(
                "En-passant setup is reached",
                setup.success(),
                "White pawn is on E5, black pawn is on D5, and White is to move immediately after D7->D5.",
                setup.details()
        );

        MoveAttempt enPassant = attemptMove(game, "e5", "d6");
        report.check(
                "En-passant move is accepted",
                enPassant.success(),
                "E5->D6 is legal immediately after D7->D5.",
                enPassant.details()
        );
        report.checkPiece(game, "White pawn ends on D6", "d6", PieceColor.WHITE, PieceType.PAWN);
        report.checkEmpty(game, "Captured black pawn is removed from D5", "d5");
        report.checkEmpty(game, "E5 is empty after en passant", "e5");

        report.finish();
    }

    @Test
    @DisplayName("08 - En passant expires after one reply")
    void enPassantExpiresAfterInterveningMove() {
        MatchReport report = new MatchReport(
                "EN PASSANT EXPIRATION",
                "Verify that en passant is no longer legal if White makes another move first."
        );
        GameSession game = newGame();

        SequenceResult sequence = playSequence(game,
                "e2", "e4",
                "a7", "a6",
                "e4", "e5",
                "d7", "d5",
                "h2", "h3",
                "a6", "a5"
        );
        report.check(
                "Expired-en-passant position is reached",
                sequence.success(),
                "The D7->D5 move is followed by one White move and one Black move before White considers E5xD6.",
                sequence.details()
        );

        Set<String> legalTargets = targetNames(game, "e5");
        report.check(
                "Expired en passant is rejected",
                !legalTargets.contains("D6"),
                "D6 must NOT be a legal target because en passant is only available on the immediately following move.",
                "Legal targets from E5 were " + legalTargets + "."
        );

        report.finish();
    }

    @Test
    @DisplayName("09 - Pawn initial two-square move")
    void pawnCanMoveTwoSquaresOnlyFromItsStartMove() {
        MatchReport report = new MatchReport(
                "PAWN TWO-SQUARE START MOVE",
                "Verify E2->E4 and E7->E5 are initially legal, then verify another two-square pawn jump is not offered."
        );
        GameSession game = newGame();

        Set<String> whiteInitial = targetNames(game, "e2");
        Set<String> blackInitial = targetNames(game, "e7");

        report.check(
                "White pawn has its two-square start move",
                whiteInitial.contains("E4"),
                "E4 is a legal target for the unmoved white pawn on E2.",
                "Legal targets from E2 were " + whiteInitial + "."
        );
        report.check(
                "Black pawn has its two-square start move",
                blackInitial.contains("E5"),
                "E5 is a legal target for the unmoved black pawn on E7.",
                "Legal targets from E7 were " + blackInitial + "."
        );

        SequenceResult sequence = playSequence(game,
                "e2", "e4",
                "e7", "e5"
        );
        report.check(
                "Both initial two-square moves execute successfully",
                sequence.success(),
                "White reaches E4 and Black reaches E5 using their initial two-square moves.",
                sequence.details()
        );

        Set<String> whiteAfterMove = targetNames(game, "e4");
        Set<String> blackAfterMove = targetNames(game, "e5");
        report.check(
                "White cannot make another two-square pawn jump",
                !whiteAfterMove.contains("E6"),
                "E6 is not offered from E4 after the pawn has already moved.",
                "Legal targets from E4 were " + whiteAfterMove + "."
        );
        report.check(
                "Black cannot make another two-square pawn jump",
                !blackAfterMove.contains("E3"),
                "E3 is not offered from E5 after the pawn has already moved.",
                "Legal targets from E5 were " + blackAfterMove + "."
        );

        report.finish();
    }

    @Test
    @DisplayName("10 - Legal movement generation for every pieceMoved type and color")
    void legalMovesAreGeneratedForEveryPieceTypeAndColor() {
        MatchReport report = new MatchReport(
                "MOVE GENERATION - ALL PIECES / BOTH COLORS",
                "Open selected files and diagonals, then compare exact legal target sets for pawn, knight, bishop, rook, queen and king for White and Black."
        );
        GameSession game = newGame();

        SequenceResult setup = playSequence(game,
                "e2", "e4",
                "e7", "e6",
                "a2", "a4",
                "a7", "a5",
                "d2", "d4",
                "d7", "d6",
                "g1", "f3",
                "g8", "f6"
        );
        report.check(
                "Movement-test position is reached",
                setup.success(),
                "The position opens the A-file, D-file and E-file while moving both G-knights away.",
                setup.details()
        );

        report.checkTargets(game, "White pawn movement", "e4", "e5");
        report.checkTargets(game, "Black pawn movement", "e6", "e5");

        report.checkTargets(game, "White knight movement", "b1", "a3", "c3", "d2");
        report.checkTargets(game, "Black knight movement", "b8", "a6", "c6", "d7");

        report.checkTargets(game, "White bishop movement", "c1", "d2", "e3", "f4", "g5", "h6");
        report.checkTargets(game, "Black bishop movement", "c8", "d7");

        report.checkTargets(game, "White rook movement", "a1", "a2", "a3");
        report.checkTargets(game, "Black rook movement", "a8", "a7", "a6");

        report.checkTargets(game, "White queen movement", "d1", "d2", "d3", "e2");
        report.checkTargets(game, "Black queen movement", "d8", "d7", "e7");

        report.checkTargets(game, "White king movement", "e1", "d2", "e2");
        report.checkTargets(game, "Black king movement", "e8", "d7", "e7");

        report.finish();
    }

    private static GameSession newGame() {
        GameSession game = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        game.setDebug(true);
        return game;
    }

    /**
     * Executes a sequence expressed as from/to pairs. The helper stops on the
     * first illegal move so later failures are not confused by a corrupted setup.
     */
    private static SequenceResult playSequence(GameSession game, String... squares) {
        if (squares.length % 2 != 0) {
            return new SequenceResult(false, "The test sequence contains an unmatched source/destination square.");
        }

        int moveNumber = 0;
        for (int i = 0; i < squares.length; i += 2) {
            moveNumber++;
            MoveAttempt attempt = attemptMove(game, squares[i], squares[i + 1]);
            if (!attempt.success()) {
                return new SequenceResult(
                        false,
                        "Move " + moveNumber + " (" + squares[i].toUpperCase() + "->" + squares[i + 1].toUpperCase() + ") failed: " + attempt.details()
                );
            }
        }

        return new SequenceResult(
                true,
                "All " + moveNumber + " moves were accepted. Current status=" + game.status() + ", turn=" + game.colorTurn() + "."
        );
    }

    /**
     * Finds the Move object that GameSession itself exposes for a target tile,
     * then sends that exact Move back through GameSession.move(...).
     */
    private static MoveAttempt attemptMove(GameSession game, String fromName, String toName) {
        Tile from = new Tile(fromName);
        Tile to = new Tile(toName);
        Board board = boardOf(game);
        Piece piece = board.pieceAt(from);

        if (piece == null) {
            return new MoveAttempt(false, "There is no pieceMoved on " + from.name() + ".");
        }

        if (piece.color() != game.colorTurn()) {
            return new MoveAttempt(
                    false,
                    "It is " + game.colorTurn() + "'s turn, but " + from.name() + " contains a " + piece.color() + " " + piece.type() + "."
            );
        }

        try {
            Map<Move, List<Tile>> possibleMoves = game.pieceMovesetTiles(from);
            Optional<Move> selectedMove = possibleMoves.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(to))
                    .map(Map.Entry::getKey)
                    .findFirst();

            if (selectedMove.isEmpty()) {
                return new MoveAttempt(
                        false,
                        to.name() + " was not legal from " + from.name() + ". Legal targets were " + targetNames(possibleMoves) + "."
                );
            }

            PieceColor movingColor = piece.color();
            PieceType movingType = piece.type();
            game.move(selectedMove.get(), from, to);

            Piece arrived = board.pieceAt(to);
            if (arrived == null || arrived.color() != movingColor || arrived.type() != movingType) {
                return new MoveAttempt(
                        false,
                        "GameSession accepted the move, but destination " + to.name() + " contains " + describe(arrived) + "."
                );
            }

            return new MoveAttempt(
                    true,
                    movingColor + " " + movingType + " moved " + from.name() + "->" + to.name() + "; status=" + game.status() + "."
            );
        } catch (RuntimeException exception) {
            return new MoveAttempt(
                    false,
                    "Move generation/execution threw " + exception.getClass().getSimpleName() + ": " + exception.getMessage()
            );
        }
    }

    private static Set<String> targetNames(GameSession game, String fromName) {
        Tile from = new Tile(fromName);
        Piece piece = boardOf(game).pieceAt(from);
        if (piece == null) {
            return Set.of();
        }

        try {
            return targetNames(game.pieceMovesetTiles(from));
        } catch (RuntimeException exception) {
            return Set.of("<EXCEPTION:" + exception.getClass().getSimpleName() + ">");
        }
    }

    private static Set<String> targetNames(Map<Move, List<Tile>> possibleMoves) {
        return possibleMoves.values().stream()
                .flatMap(List::stream)
                .map(Tile::name)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Set<String> expectedTargets(String... targets) {
        return Arrays.stream(targets)
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Board boardOf(GameSession game) {
        try {
            return (Board) BOARD_FIELD.get(game);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not inspect GameSession.board for test verification.", exception);
        }
    }

    private static Field boardField() {
        try {
            Field field = GameSession.class.getDeclaredField("board");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static String describe(Piece piece) {
        return piece == null ? "empty" : piece.color() + " " + piece.type();
    }

    private record MoveAttempt(boolean success, String details) {
    }

    private record SequenceResult(boolean success, String details) {
    }

    private record ObjectiveResult(
            String objective,
            boolean accomplished,
            String expected,
            String actual
    ) {
    }

    private static final class MatchReport {
        private final String title;
        private final String purpose;
        private final List<ObjectiveResult> objectives = new ArrayList<>();

        private MatchReport(String title, String purpose) {
            this.title = title;
            this.purpose = purpose;

            System.out.println();
            System.out.println("============================================================");
            System.out.println("MATCH TEST: " + title);
            System.out.println("TRIES TO TEST: " + purpose);
            System.out.println("============================================================");
        }

        private void check(String objective, boolean accomplished, String expected, String actual) {
            objectives.add(new ObjectiveResult(objective, accomplished, expected, actual));
        }

        private void checkPiece(
                GameSession game,
                String objective,
                String square,
                PieceColor expectedColor,
                PieceType expectedType
        ) {
            Piece actualPiece = boardOf(game).pieceAt(new Tile(square));
            boolean correct = actualPiece != null
                    && actualPiece.color() == expectedColor
                    && actualPiece.type() == expectedType;

            check(
                    objective,
                    correct,
                    expectedColor + " " + expectedType + " on " + square.toUpperCase() + ".",
                    square.toUpperCase() + " contained " + describe(actualPiece) + "."
            );
        }

        private void checkEmpty(GameSession game, String objective, String square) {
            Piece actualPiece = boardOf(game).pieceAt(new Tile(square));
            check(
                    objective,
                    actualPiece == null,
                    square.toUpperCase() + " is empty.",
                    square.toUpperCase() + " contained " + describe(actualPiece) + "."
            );
        }

        private void checkTargets(GameSession game, String objective, String from, String... expected) {
            Set<String> expectedSet = expectedTargets(expected);
            Set<String> actualSet = targetNames(game, from);
            check(
                    objective,
                    actualSet.equals(expectedSet),
                    "Legal targets from " + from.toUpperCase() + " are exactly " + expectedSet + ".",
                    "Legal targets from " + from.toUpperCase() + " were " + actualSet + "."
            );
        }

        private void finish() {
            long accomplished = objectives.stream().filter(ObjectiveResult::accomplished).count();
            long unmet = objectives.size() - accomplished;

            System.out.println();
            System.out.println("OBJECTIVE REPORT - " + title);
            System.out.println("------------------------------------------------------------");

            for (ObjectiveResult objective : objectives) {
                if (objective.accomplished()) {
                    System.out.println("[ACCOMPLISHED] " + objective.objective());
                } else {
                    System.out.println("[NOT ACCOMPLISHED] " + objective.objective());
                    System.out.println("  Expected: " + objective.expected());
                    System.out.println("  Actual:   " + objective.actual());
                }
            }

            System.out.println("------------------------------------------------------------");
            System.out.println("Accomplished: " + accomplished + "/" + objectives.size());
            System.out.println("Unmet:        " + unmet + "/" + objectives.size());
            System.out.println("============================================================");

            List<Executable> failures = objectives.stream()
                    .filter(objective -> !objective.accomplished())
                    .map(objective -> (Executable) () -> fail(
                            objective.objective()
                                    + System.lineSeparator()
                                    + "Expected: " + objective.expected()
                                    + System.lineSeparator()
                                    + "Actual:   " + objective.actual()
                    ))
                    .toList();

            assertAll(title, failures);
        }
    }
}
