import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reivajh06.archchess.architecture.domain.services.FenParser;
import reivajh06.archchess.architecture.domain.model.game.Difficulty;
import reivajh06.archchess.architecture.domain.model.game.GameConditions;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.architecture.domain.model.game.GameStatus;
import reivajh06.archchess.architecture.domain.logic.GameDirector;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;
import reivajh06.archchess.architecture.domain.model.Board;
import reivajh06.archchess.architecture.domain.model.Piece;
import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.PieceType;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.movement.Move;
import reivajh06.archchess.architecture.domain.model.movement.MoveEvent;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class GameSimulationTest {

    private static final String START =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static GameSession newGame() {
        return new GameSession(Gamemode.PVP, Difficulty.NOT_PLUGIN);
    }

    private static GameSession at(String fen) {
        return ChessTestSupport.sessionAt(fen);
    }

    private static void play(GameSession session, String moves) {
        String[] tokens = moves.trim().split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            String uci = tokens[i];
            String before = bestEffortFen(session);
            int oldPly = session.gameState().movesHistory().size();
            try {
                Color side = session.gameState().turnColor();
                ChessTestSupport.playMove(session, uci);
                assertEquals(oldPly + 1, session.gameState().movesHistory().size(), "one event per accepted move");
                assertEquals(Color.rivalColor(side), session.gameState().turnColor(), "turn must alternate");
                assertEquals(uci.substring(2, 4),
                        session.gameState().movesHistory().getFirst().to().name().toLowerCase(Locale.ROOT),
                        "recorded destination");
            } catch (RuntimeException | AssertionError failure) {
                throw new AssertionError("Ply " + (i + 1) + ", move " + uci
                        + ", position BEFORE move: " + before, failure);
            }
        }
    }

    private static String bestEffortFen(GameSession session) {
        try { return session.toFEN(); }
        catch (RuntimeException e) { return "<bad full FEN: " + e + "> " + FenParser.boardToFen(session.gameState().board()); }
    }

    private static Piece assertPiece(GameSession session, String square, Color color, PieceType type) {
        Piece piece = session.gameState().board().pieceAt(new Tile(square));
        assertNotNull(piece, "missing piece on " + square);
        assertEquals(color, piece.color(), "color on " + square);
        assertEquals(type, piece.type(), "type on " + square);
        return piece;
    }

    private static void assertEmpty(GameSession session, String square) {
        assertNull(session.gameState().board().pieceAt(new Tile(square)), "expected empty " + square);
    }


    private static void assertMoveUnavailable(GameSession session, String from, String to) {
        Tile destination = new Tile(to);
        Map<Integer, List<Tile>> legalMoves = new GameDirector(session).pieceMoves(new Tile(from));
        boolean available = legalMoves.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(destination::equals);
        assertFalse(available, from + to + " must not be returned as a legal move");
    }

    @Test
    @DisplayName("An opening updates turns, move counters and history")
    void openingAndFenCounters() {
        GameSession session = newGame();
        assertEquals(START, session.toFEN());
        play(session, "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6");
        assertEquals(GameStatus.ACTIVE, session.gameState().status());
        assertEquals(4, session.gameState().fullMoves());
        assertEquals(0, session.gameState().halfMoveClock());
        assertEquals(6, session.gameState().movesHistory().size());
    }

    static Stream<Arguments> pawnPushFens() {
        return Stream.of(
                Arguments.of("e2e4",
                        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"),
                Arguments.of("e2e4 e7e5",
                        "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2")
        );
    }

    @ParameterizedTest(name = "FEN after {0}")
    @MethodSource("pawnPushFens")
    void pawnPushProducesStandardFen(String moves, String expectedFen) {
        GameSession session = newGame();
        play(session, moves);
        assertEquals(expectedFen, session.toFEN());
    }

    static Stream<Arguments> decisiveMatches() {
        return Stream.of(
                Arguments.of("Fool's Mate / Black wins", START,
                        "f2f3 e7e5 g2g4 d8h4", GameStatus.BLACK_WIN, Color.WHITE),
                Arguments.of("Scholar's Mate / White wins", START,
                        "e2e4 e7e5 f1c4 b8c6 d1h5 g8f6 h5f7", GameStatus.WHITE_WIN, Color.BLACK),
                Arguments.of("Back-rank rook mate / White wins", "6k1/5ppp/8/8/8/8/8/4R1K1 w - - 0 1",
                        "e1e8", GameStatus.WHITE_WIN, Color.BLACK)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("decisiveMatches")
    void winsByCheckmate(String name, String fen, String moves,
                         GameStatus expected, Color checked) {
        GameSession session = at(fen);
        play(session, moves);
        assertAll(
                () -> assertEquals(expected, session.gameState().status(), name),
                () -> assertEquals(checked, session.gameState().checkedColor(), "losing king is in check"),
                () -> assertTrue(GameConditions.isCheckMate(session.gameState(), checked), "position is checkmate")
        );
    }

    @Test
    @DisplayName("Stalemate reached by a real queen move is a draw, not checkmate")
    void drawByStalemate() {
        GameSession session = at("k7/2Q5/2K5/8/8/8/8/8 w - - 0 1");
        play(session, "c7b6");
        assertAll(
                () -> assertFalse(GameConditions.isKingChecked(session.gameState(), Color.BLACK)),
                () -> assertTrue(GameConditions.noLegalMoves(session.gameState(), Color.BLACK)),
                () -> assertEquals(GameStatus.DRAW, session.gameState().status())
        );
    }

    @Test
    @DisplayName("Capturing the last minor piece reaches a dead position")
    void drawByInsufficientMaterialAfterCapture() {
        GameSession session = at("4k3/8/8/8/8/8/3n4/4K3 w - - 0 1");
        play(session, "e1d2");
        assertPiece(session, "d2", Color.WHITE, PieceType.KING);
        assertEquals(GameStatus.DRAW, session.gameState().status());
        assertTrue(GameConditions.onDeadPosition(session.gameState()));
    }

    @Test
    @DisplayName("Threefold repetition is claimable, not an automatic terminal draw")
    void threefoldIsNotAutomaticallyDraw() {
        GameSession session = newGame();
        play(session, "g1f3 g8f6 f3g1 f6g8 g1f3 g8f6 f3g1 f6g8");
        assertEquals(GameStatus.ACTIVE, session.gameState().status());
        assertEquals(8, session.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Fivefold repetition automatically ends the match in a draw")
    void fivefoldRepetitionDraw() {
        GameSession session = newGame();
        String cycle = "g1f3 g8f6 f3g1 f6g8";
        play(session, cycle + " " + cycle + " " + cycle + " " + cycle);
        assertEquals(GameStatus.DRAW, session.gameState().status());
        assertEquals(16, session.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Fifty-move rule threshold is a claim, not automatic termination")
    void fiftyMoveThresholdIsClaimable() {
        GameSession session = at("4k2r/8/8/8/8/8/8/R3K3 w Qk - 99 50");
        play(session, "a1a2");
        assertEquals(100, session.gameState().halfMoveClock());
        assertEquals(GameStatus.ACTIVE, session.gameState().status());
    }

    @Test
    @DisplayName("75-move rule automatically draws without pawn moves or captures")
    void seventyFiveMoveAutomaticDraw() {
        GameSession session = at("4k2r/8/8/8/8/8/8/R3K3 w Qk - 149 75");
        play(session, "a1a2");
        assertEquals(150, session.gameState().halfMoveClock());
        assertEquals(GameStatus.DRAW, session.gameState().status());
    }

    @Test
    @DisplayName("Checkmate takes precedence over the automatic 75-move draw")
    void checkmateOverridesSeventyFiveMoveRule() {
        GameSession session = at("6k1/5ppp/8/8/8/8/8/4R1K1 w - - 149 75");
        play(session, "e1e8");
        assertEquals(150, session.gameState().halfMoveClock());
        assertEquals(GameStatus.WHITE_WIN, session.gameState().status());
    }

    @Test
    @DisplayName("Both players castle kingside; king, rook, and rights remain consistent")
    void doubleKingsideCastling() {
        GameSession session = newGame();
        play(session, "g1f3 g8f6 g2g3 g7g6 f1g2 f8g7 e1g1 e8g8");
        assertPiece(session, "g1", Color.WHITE, PieceType.KING);
        assertPiece(session, "f1", Color.WHITE, PieceType.ROOK);
        assertPiece(session, "g8", Color.BLACK, PieceType.KING);
        assertPiece(session, "f8", Color.BLACK, PieceType.ROOK);
        assertEmpty(session, "e1");
        assertEmpty(session, "e8");
        assertEquals("-", session.toFEN().split(" ")[2], "no remaining castling rights");
    }

    @Test
    @DisplayName("White queenside castle correctly moves both pieces")
    void queensideCastling() {
        GameSession session = newGame();
        play(session, "d2d4 d7d5 b1c3 g8f6 c1f4 e7e6 d1d2 f8e7 e1c1");
        assertPiece(session, "c1", Color.WHITE, PieceType.KING);
        assertPiece(session, "d1", Color.WHITE, PieceType.ROOK);
        assertEmpty(session, "a1");
        assertEmpty(session, "e1");
        assertFalse(session.toFEN().split(" ")[2].contains("K"));
        assertFalse(session.toFEN().split(" ")[2].contains("Q"));
    }

    @Test
    @DisplayName("Returning a moved rook to its corner never restores castling rights")
    void movedRooksLoseRightsPermanently() {
        GameSession session = newGame();
        play(session, "h2h4 a7a5 h1h3 a8a6 h3h1");
        String rights = session.toFEN().split(" ")[2];
        assertFalse(rights.contains("K"), "white kingside revoked");
        assertFalse(rights.contains("q"), "black queenside revoked");
        assertTrue(rights.contains("Q"), "white queenside retained");
        assertTrue(rights.contains("k"), "black kingside retained");
    }

    @Test
    @DisplayName("Capturing an unmoved rook also removes that side's castling right")
    void capturedRookRemovesOpponentRights() {
        GameSession session = at("r3k2r/8/8/8/8/8/6B1/R3K2R w KQkq - 0 1");
        play(session, "g2a8");
        assertPiece(session, "a8", Color.WHITE, PieceType.BISHOP);
        assertEquals("KQk", session.toFEN().split(" ")[2]);
    }

    @Test
    @DisplayName("En passant removes the adjacent pawn and expires its target")
    void enPassantCapture() {
        GameSession session = newGame();
        play(session, "e2e4 a7a6 e4e5 d7d5");
        play(session, "e5d6");
        assertPiece(session, "d6", Color.WHITE, PieceType.PAWN);
        assertEmpty(session, "d5");
        assertNull(session.gameState().enPassantDestination(), "en passant expires immediately after capture");
        assertEquals(0, session.gameState().halfMoveClock());
        assertEquals(1, session.gameState().eatenPieces().get(Color.BLACK).size());
    }

    @Test
    @DisplayName("A double push records the passed square as its en passant target")
    void enPassantTargetAfterDoublePush() {
        GameSession session = newGame();
        play(session, "e2e4 a7a6 e4e5 d7d5");
        assertEquals(new Tile("d6"), session.gameState().enPassantDestination());
    }

    @Test
    @DisplayName("En passant cannot be played two turns after the double push")
    void expiredEnPassantRejected() {
        GameSession session = newGame();
        play(session, "e2e4 a7a6 e4e5 d7d5 g1f3 g8f6");
        assertMoveUnavailable(session, "e5", "d6");
    }

    @Test
    @DisplayName("En passant that exposes own king to rook check is illegal")
    void enPassantDiscoveredCheckRejected() {
        GameSession session = at("4r1k1/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
        Piece blackPawn = assertPiece(session, "d5", Color.BLACK, PieceType.PAWN);
        Move doublePush = blackPawn.moveset().stream()
                .filter(m -> m.x() == 0 && m.y() == 2).findFirst().orElseThrow();
        session.gameState().movesHistory().addFirst(new MoveEvent(
                blackPawn, doublePush, new Tile("d7"), new Tile("d5"), null,
                "4r1k1/8/8/3pP3/8/8/8/4K3 w - d6 0 1"));
        assertMoveUnavailable(session, "e5", "d6");
    }

    static Stream<Color> promotionColors() {
        return Stream.of(Color.WHITE, Color.BLACK);
    }

    // Coronar va en dos pasos: la jugada lleva el peón a la última fila y /promote (promotePiece) lo cambia
    // por la pieza que elija el jugador.
    @ParameterizedTest(name = "{0} pawn reaching the last rank is promoted in a second step")
    @MethodSource("promotionColors")
    void pawnIsPromotedAfterReachingTheLastRank(Color color) {
        String initial = color == Color.WHITE
                ? "7k/P7/6K1/8/8/8/8/8 w - - 0 1"
                : "8/8/8/8/8/6k1/p7/7K b - - 0 1";
        String target = color == Color.WHITE ? "a8" : "a1";
        String uci = color == Color.WHITE ? "a7a8" : "a2a1";
        GameSession session = at(initial);

        ChessTestSupport.playMove(session, uci);
        Piece arrived = session.gameState().board().pieceAt(new Tile(target));
        assertNotNull(arrived);
        assertEquals(PieceType.PAWN, arrived.type(), "the move itself only carries the pawn to the last rank");
        assertEquals(1, session.gameState().movesHistory().size());

        new GameDirector(session).promotePiece(new Tile(target), PieceType.QUEEN);
        Piece promoted = session.gameState().board().pieceAt(new Tile(target));
        assertNotNull(promoted);
        assertEquals(color, promoted.color());
        assertEquals(PieceType.QUEEN, promoted.type(), "the chosen piece replaces the pawn");
    }

    @Test
    @DisplayName("Promotion is ignored for a piece that is not a pawn on its last rank")
    void promotionRequiresAPawnOnTheLastRank() {
        GameSession session = at("7k/P7/6K1/8/8/8/8/8 w - - 0 1");
        String before = session.toFEN();
        new GameDirector(session).promotePiece(new Tile("a7"), PieceType.QUEEN);
        new GameDirector(session).promotePiece(new Tile("g6"), PieceType.QUEEN);
        assertEquals(before, session.toFEN());
    }

    @Test
    @DisplayName("Pinned rook cannot expose its own king")
    void pinnedPieceCannotMoveSideways() {
        GameSession session = at("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1");
        assertMoveUnavailable(session, "e2", "d2");
    }

    @Test
    @DisplayName("King cannot move into rook attack")
    void kingCannotEnterCheck() {
        GameSession session = at("4k3/8/8/8/8/8/3r4/4K3 w - - 0 1");
        assertMoveUnavailable(session, "e1", "d1");
    }

    @Test
    @DisplayName("Castling through an attacked transit square is illegal")
    void cannotCastleThroughCheck() {
        GameSession session = at("r3k2r/8/8/8/8/5r2/8/R3K2R w KQkq - 0 1");
        assertMoveUnavailable(session, "e1", "g1");
    }

    @Test
    @DisplayName("Castling while the king starts in check is illegal")
    void cannotCastleOutOfCheck() {
        GameSession session = at("4r1k1/8/8/8/8/8/8/4K2R w K - 0 1");
        assertMoveUnavailable(session, "e1", "g1");
    }

    @Test
    @DisplayName("A missing rook prevents castling even if a stale rights flag remains")
    void cannotCastleWithMissingRook() {
        GameSession session = at("4k3/8/8/8/8/8/8/4K3 w K - 0 1");
        assertMoveUnavailable(session, "e1", "g1");
    }

    @Test
    @DisplayName("Move history behaves as a stack: newest accepted event is at the head")
    void moveHistoryIsNewestFirstStack() {
        GameSession session = newGame();
        play(session, "e2e4 e7e5 g1f3");

        Deque<MoveEvent> history = session.gameState().movesHistory();
        assertEquals(3, history.size());
        assertSame(history.peekFirst(), session.gameState().lastMove(), "lastMove must return the stack top");
        assertEquals(new Tile("g1"), history.getFirst().from());
        assertEquals(new Tile("f3"), history.getFirst().to());
        assertEquals(new Tile("e2"), history.getLast().from(), "oldest event remains at the tail");
        assertEquals(new Tile("e4"), history.getLast().to());
    }

    @Test
    @DisplayName("An accepted capture updates the board and both captured-piece registry/history")
    void captureConservesPieceCounts() {
        GameSession session = newGame();
        play(session, "e2e4 d7d5 e4d5");
        assertPiece(session, "d5", Color.WHITE, PieceType.PAWN);
        assertEquals(31, occupiedSquares(session.gameState().board()));
        assertEquals(1, session.gameState().eatenPieces().get(Color.BLACK).size());
        assertEquals(PieceType.PAWN, session.gameState().movesHistory().getFirst().pieceCaptured().type());
    }

    @Test
    @DisplayName("Stored move event contains a reconstructible complete position after each ply")
    void historyContainsFullFenForEveryPosition() {
        GameSession session = newGame();
        play(session, "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6");
        Deque<MoveEvent> events = session.gameState().movesHistory();
        assertEquals(8, events.size());
        assertSame(events.getFirst(), session.gameState().lastMove(), "lastMove must read history head/top");
        for (MoveEvent event : events) {
            String stored = event.gameStateFen();
            assertEquals(6, stored.trim().split("\\s+").length,
                    "event must record the complete six-field position after " + event.from() + event.to());
            GameSession reconstructed = at(stored);
            assertEquals(stored, reconstructed.toFEN(), "stored position must be independently loadable");
        }
        assertEquals(session.toFEN(), events.getFirst().gameStateFen(), "top event must be the current position");
    }

    @Test
    @DisplayName("Replay full event history from starting position reconstructs the current session")
    void replayEventSequenceMatchesFinalPosition() {
        String transcript = "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6 e1g1 f8e7";
        GameSession original = newGame();
        play(original, transcript);
        GameSession replay = newGame();
        Iterator<MoveEvent> chronological = original.gameState().movesHistory().descendingIterator();
        while (chronological.hasNext()) {
            MoveEvent e = chronological.next();
            ChessTestSupport.playMove(replay,
                    e.from().name().toLowerCase(Locale.ROOT) + e.to().name().toLowerCase(Locale.ROOT));
        }
        assertEquals(original.toFEN(), replay.toFEN());
        assertEquals(original.gameState().status(), replay.gameState().status());
        assertEquals(original.gameState().movesHistory().size(), replay.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Legal move enumeration must not alter king caches, board, clocks or history")
    void repeatedLegalMoveQueryIsPure() {
        GameSession session = newGame();
        String initialFen = session.toFEN();
        int initialCount = session.gameState().movesHistory().size();
        int pawnMoves = session.gameState().board().pieceAt(new Tile("e2")).timesMoved();
        for (int i = 0; i < 3; i++) {
            new GameDirector(session).pieceMoves(new Tile("e2"));
            new GameDirector(session).pieceMoves(new Tile("e1"));
            new GameDirector(session).pieceMoves(new Tile("g1"));
        }
        assertEquals(initialFen, session.toFEN());
        assertEquals(initialCount, session.gameState().movesHistory().size());
        assertEquals(pawnMoves, session.gameState().board().pieceAt(new Tile("e2")).timesMoved());
        assertEquals(new Tile("e1"), session.gameState().board().kingTile(Color.WHITE));
    }

    @Test
    @DisplayName("An illegal capture leaves the board and metadata unchanged")
    void illegalCaptureDoesNotCorruptSession() {
        GameSession session = newGame();
        play(session, "e2e4 d7d5");

        String before = session.toFEN();
        int count = session.gameState().movesHistory().size();
        assertThrows(IllegalArgumentException.class,
                () -> ChessTestSupport.playMove(session, "e4d6"));
        assertEquals(before, session.toFEN());
        assertEquals(count, session.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Two game sessions must have isolated boards, histories and player turns")
    void separateSessionsHaveIndependentState() {
        GameSession first = newGame();
        GameSession second = newGame();
        play(first, "e2e4 e7e5");
        play(second, "d2d4 d7d5");
        assertNotEquals(first.id(), second.id());
        assertNotEquals(first.toFEN(), second.toFEN());
        assertEquals(2, first.gameState().movesHistory().size());
        assertEquals(2, second.gameState().movesHistory().size());
        assertPiece(first, "e4", Color.WHITE, PieceType.PAWN);
        assertPiece(second, "d4", Color.WHITE, PieceType.PAWN);
        assertNull(second.gameState().board().pieceAt(new Tile("e4")));
    }

    @Test
    @DisplayName("FEN import contains only the supplied pieces and tracks both kings")
    void productionBoardFenImportDoesNotOverlayStartingPosition() {
        Board loaded = FenParser.fenToBoard("4k3/8/8/8/8/8/8/4K3");
        assertEquals("4k3/8/8/8/8/8/8/4K3", FenParser.boardToFen(loaded));
        assertEquals(2, occupiedSquares(loaded));
        assertEquals(new Tile("e1"), loaded.kingTile(Color.WHITE));
        assertEquals(new Tile("e8"), loaded.kingTile(Color.BLACK));
    }

    private static int occupiedSquares(Board board) {
        return (int) board.asStream().filter(tile -> board.pieceAt(tile) != null).count();
    }
}
