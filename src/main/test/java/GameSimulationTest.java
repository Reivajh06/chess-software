import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reivajh06.gamelogic.*;
import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveEvent;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GameSimulationTest {

    private static final String START =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static GameSession newGame() {
        return new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
    }

    private static GameSession at(String fen) {
        GameSession session = newGame();

        ChessDebugCli.loadFen(session, fen);
        return session;
    }

    private static void play(GameSession session, String moves) {
        String[] tokens = moves.trim().split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            String uci = tokens[i];
            String before = bestEffortFen(session);
            int oldPly = session.gameState().movesHistory().size();
            try {
                PieceColor side = session.gameState().turnColor();
                ChessDebugCli.applyCoordinateMove(session, uci);
                assertEquals(oldPly + 1, session.gameState().movesHistory().size(), "one event per accepted move");
                assertEquals(PieceColor.rivalColor(side), session.gameState().turnColor(), "turn must alternate");
                assertEquals(uci.substring(2, 4),
                        session.gameState().movesHistory().getFirst().to().name().toLowerCase(Locale.ROOT),
                        "recorded destination");
            } catch (Throwable failure) {
                throw new AssertionError("Ply " + (i + 1) + ", move " + uci
                        + ", position BEFORE move: " + before, failure);
            }
        }
    }

    private static String bestEffortFen(GameSession session) {
        try { return session.toFEN(); }
        catch (RuntimeException e) { return "<bad full FEN: " + e + "> " + session.gameState().board().toFEN(); }
    }

    private static Piece assertPiece(GameSession s, String square, PieceColor color, PieceType type) {
        Piece piece = s.gameState().board().pieceAt(new Tile(square));
        assertNotNull(piece, "missing piece on " + square);
        assertEquals(color, piece.color(), "color on " + square);
        assertEquals(type, piece.type(), "type on " + square);
        return piece;
    }

    private static void assertEmpty(GameSession s, String square) {
        assertNull(s.gameState().board().pieceAt(new Tile(square)), "expected empty " + square);
    }


    private static void assertMoveUnavailable(GameSession s, String from, String to) {
        Tile destination = new Tile(to);
        Map<Move, List<Tile>> legalMoves = new GameUpdater(s).pieceMoves(new Tile(from));
        boolean available = legalMoves.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(destination::equals);
        assertFalse(available, from + to + " must not be returned as a legal move");
    }

    @Test
    @DisplayName("Opening, counters, and exact full FEN after each pawn push")
    void openingAndFenCounters() {
        GameSession s = newGame();
        assertEquals(START, s.toFEN());
        play(s, "e2e4");
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", s.toFEN());
        play(s, "e7e5");
        assertEquals("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2", s.toFEN());
        play(s, "g1f3 b8c6 f1b5 a7a6");
        assertEquals(GameStatus.ACTIVE, s.gameState().status());
        assertEquals(4, s.gameState().fullMoves());
        assertEquals(0, s.gameState().halfMoveClock());
        assertEquals(6, s.gameState().movesHistory().size());
    }

    static Stream<Arguments> decisiveMatches() {
        return Stream.of(
                Arguments.of("Fool's Mate / Black wins", START,
                        "f2f3 e7e5 g2g4 d8h4", GameStatus.BLACK_WIN, PieceColor.WHITE),
                Arguments.of("Scholar's Mate / White wins", START,
                        "e2e4 e7e5 f1c4 b8c6 d1h5 g8f6 h5f7", GameStatus.WHITE_WIN, PieceColor.BLACK),
                Arguments.of("Back-rank rook mate / White wins", "6k1/5ppp/8/8/8/8/8/4R1K1 w - - 0 1",
                        "e1e8", GameStatus.WHITE_WIN, PieceColor.BLACK)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("decisiveMatches")
    void winsByCheckmate(String name, String fen, String moves,
                         GameStatus expected, PieceColor checked) {
        GameSession s = at(fen);
        play(s, moves);
        assertEquals(expected, s.gameState().status(), name);
        assertEquals(checked, s.gameState().checkedColor(), "losing king is in check");
        assertTrue(GameConditions.isCheckMate(s.gameState(), checked), "position is checkmate");
    }

    @Test
    @DisplayName("Stalemate reached by a real queen move is a draw, not checkmate")
    void drawByStalemate() {
        GameSession s = at("k7/2Q5/2K5/8/8/8/8/8 w - - 0 1");
        play(s, "c7b6");
        assertFalse(GameConditions.isKingChecked(s.gameState(), PieceColor.BLACK));
        assertTrue(GameConditions.noLegalMoves(s.gameState(), PieceColor.BLACK));
        assertEquals(GameStatus.DRAW, s.gameState().status());
    }

    @Test
    @DisplayName("Capturing the last minor piece reaches a dead position")
    void drawByInsufficientMaterialAfterCapture() {
        GameSession s = at("4k3/8/8/8/8/8/3n4/4K3 w - - 0 1");
        play(s, "e1d2");
        assertPiece(s, "d2", PieceColor.WHITE, PieceType.KING);
        assertEquals(GameStatus.DRAW, s.gameState().status());
        assertTrue(GameConditions.onDeadPosition(s.gameState()));
    }

    @Test
    @DisplayName("Threefold repetition is claimable, not an automatic terminal draw")
    void threefoldIsNotAutomaticallyDraw() {
        GameSession s = newGame();
        play(s, "g1f3 g8f6 f3g1 f6g8 g1f3 g8f6 f3g1 f6g8");
        assertEquals(GameStatus.ACTIVE, s.gameState().status());
        assertEquals(8, s.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Fivefold repetition automatically ends the match in a draw")
    void fivefoldRepetitionDraw() {
        GameSession s = newGame();
        String cycle = "g1f3 g8f6 f3g1 f6g8";
        play(s, cycle + " " + cycle + " " + cycle + " " + cycle);
        assertEquals(GameStatus.DRAW, s.gameState().status());
        assertEquals(16, s.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Fifty-move rule threshold is a claim, not automatic termination")
    void fiftyMoveThresholdIsClaimable() {
        GameSession s = at("4k2r/8/8/8/8/8/8/R3K3 w Qk - 99 50");
        play(s, "a1a2");
        assertEquals(100, s.gameState().halfMoveClock());
        assertEquals(GameStatus.ACTIVE, s.gameState().status());
    }

    @Test
    @DisplayName("75-move rule automatically draws without pawn moves or captures")
    void seventyFiveMoveAutomaticDraw() {
        GameSession s = at("4k2r/8/8/8/8/8/8/R3K3 w Qk - 149 75");
        play(s, "a1a2");
        assertEquals(150, s.gameState().halfMoveClock());
        assertEquals(GameStatus.DRAW, s.gameState().status());
    }

    @Test
    @DisplayName("Checkmate takes precedence over the automatic 75-move draw")
    void checkmateOverridesSeventyFiveMoveRule() {
        GameSession s = at("6k1/5ppp/8/8/8/8/8/4R1K1 w - - 149 75");
        play(s, "e1e8");
        assertEquals(150, s.gameState().halfMoveClock());
        assertEquals(GameStatus.WHITE_WIN, s.gameState().status());
    }

    @Test
    @DisplayName("Both players castle kingside; king, rook, and rights remain consistent")
    void doubleKingsideCastling() {
        GameSession s = newGame();
        play(s, "g1f3 g8f6 g2g3 g7g6 f1g2 f8g7 e1g1 e8g8");
        assertPiece(s, "g1", PieceColor.WHITE, PieceType.KING);
        assertPiece(s, "f1", PieceColor.WHITE, PieceType.ROOK);
        assertPiece(s, "g8", PieceColor.BLACK, PieceType.KING);
        assertPiece(s, "f8", PieceColor.BLACK, PieceType.ROOK);
        assertEmpty(s, "e1");
        assertEmpty(s, "e8");
        assertEquals("-", s.toFEN().split(" ")[2], "no remaining castling rights");
    }

    @Test
    @DisplayName("White queenside castle correctly moves both pieces")
    void queensideCastling() {
        GameSession s = newGame();
        play(s, "d2d4 d7d5 b1c3 g8f6 c1f4 e7e6 d1d2 f8e7 e1c1");
        assertPiece(s, "c1", PieceColor.WHITE, PieceType.KING);
        assertPiece(s, "d1", PieceColor.WHITE, PieceType.ROOK);
        assertEmpty(s, "a1");
        assertEmpty(s, "e1");
        assertFalse(s.toFEN().split(" ")[2].contains("K"));
        assertFalse(s.toFEN().split(" ")[2].contains("Q"));
    }

    @Test
    @DisplayName("Returning a moved rook to its corner never restores castling rights")
    void movedRooksLoseRightsPermanently() {
        GameSession s = newGame();
        play(s, "h2h4 a7a5 h1h3 a8a6 h3h1");
        String rights = s.toFEN().split(" ")[2];
        assertFalse(rights.contains("K"), "white kingside revoked");
        assertFalse(rights.contains("q"), "black queenside revoked");
        assertTrue(rights.contains("Q"), "white queenside retained");
        assertTrue(rights.contains("k"), "black kingside retained");
    }

    @Test
    @DisplayName("Capturing an unmoved rook also removes that side's castling right")
    void capturedRookRemovesOpponentRights() {
        GameSession s = at("r3k2r/8/8/8/8/8/6B1/R3K2R w KQkq - 0 1");
        play(s, "g2a8");
        assertPiece(s, "a8", PieceColor.WHITE, PieceType.BISHOP);
        assertEquals("KQk", s.toFEN().split(" ")[2]);
    }

    @Test
    @DisplayName("En passant removes the adjacent pawn, not the destination occupant")
    void enPassantCapture() {
        GameSession s = newGame();
        play(s, "e2e4 a7a6 e4e5 d7d5");
        assertEquals(new Tile("d6"), s.gameState().enPassantDestination());
        play(s, "e5d6");
        assertPiece(s, "d6", PieceColor.WHITE, PieceType.PAWN);
        assertEmpty(s, "d5");
        assertNull(s.gameState().enPassantDestination(), "en passant expires immediately after capture");
        assertEquals(0, s.gameState().halfMoveClock());
        assertEquals(1, s.gameState().eatenPieces().get(PieceColor.BLACK).size());
    }

    @Test
    @DisplayName("En passant cannot be played two turns after the double push")
    void expiredEnPassantRejected() {
        GameSession s = newGame();
        play(s, "e2e4 a7a6 e4e5 d7d5 g1f3 g8f6");
        assertMoveUnavailable(s, "e5", "d6");
    }

    @Test
    @DisplayName("En passant that exposes own king to rook check is illegal")
    void enPassantDiscoveredCheckRejected() {
        GameSession s = at("4r1k1/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
        Piece blackPawn = assertPiece(s, "d5", PieceColor.BLACK, PieceType.PAWN);
        Move doublePush = blackPawn.moveset().stream()
                .filter(m -> m.x() == 0 && m.y() == 2).findFirst().orElseThrow();
        s.gameState().movesHistory().addFirst(new MoveEvent(
                blackPawn, doublePush, new Tile("d7"), new Tile("d5"), null,
                "4r1k1/8/8/3pP3/8/8/8/4K3 w - d6 0 1"));
        assertMoveUnavailable(s, "e5", "d6");
    }

    static Stream<Arguments> promotions() {
        return Stream.of(PieceColor.WHITE, PieceColor.BLACK).flatMap(color ->
                Stream.of(Arguments.of(color, "q", PieceType.QUEEN),
                          Arguments.of(color, "r", PieceType.ROOK),
                          Arguments.of(color, "b", PieceType.BISHOP),
                          Arguments.of(color, "n", PieceType.KNIGHT)));
    }

    @ParameterizedTest(name = "{0} promotion to {2}")
    @MethodSource("promotions")
    void allPromotionChoices(PieceColor color, String suffix, PieceType promotedType) {
        String initial = color == PieceColor.WHITE
                ? "7k/P7/6K1/8/8/8/8/8 w - - 0 1"
                : "8/8/8/8/8/6k1/p7/7K b - - 0 1";
        String target = color == PieceColor.WHITE ? "a8" : "a1";
        String uci = color == PieceColor.WHITE ? "a7a8" + suffix : "a2a1" + suffix;
        GameSession s = at(initial);

        play(s, uci);
        assertPiece(s, target, color, promotedType);
        assertEquals(1, s.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Promotion with checkmate updates the winning status immediately")
    void promotionCheckmate() {
        GameSession s = at("7k/4P3/6K1/8/8/8/8/8 w - - 0 1");
        play(s, "e7e8q");
        assertPiece(s, "e8", PieceColor.WHITE, PieceType.QUEEN);
        assertEquals(GameStatus.WHITE_WIN, s.gameState().status());
    }

    @Test
    @DisplayName("Pinned rook cannot expose its own king")
    void pinnedPieceCannotMoveSideways() {
        GameSession s = at("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1");
        assertMoveUnavailable(s, "e2", "d2");
    }

    @Test
    @DisplayName("King cannot move into rook attack")
    void kingCannotEnterCheck() {
        GameSession s = at("4k3/8/8/8/8/8/3r4/4K3 w - - 0 1");
        assertMoveUnavailable(s, "e1", "d1");
    }

    @Test
    @DisplayName("Castling through an attacked transit square is illegal")
    void cannotCastleThroughCheck() {
        GameSession s = at("r3k2r/8/8/8/8/5r2/8/R3K2R w KQkq - 0 1");
        assertMoveUnavailable(s, "e1", "g1");
    }

    @Test
    @DisplayName("Castling while the king starts in check is illegal")
    void cannotCastleOutOfCheck() {
        GameSession s = at("4r1k1/8/8/8/8/8/8/4K2R w K - 0 1");
        assertMoveUnavailable(s, "e1", "g1");
    }

    @Test
    @DisplayName("A side cannot castle without the required rook")
    void cannotCastleWithMissingRook() {
        GameSession s = at("4k3/8/8/8/8/8/8/4K3 w K - 0 1");
        assertMoveUnavailable(s, "e1", "g1");
    }

    @Test
    @DisplayName("Move history behaves as a stack: newest accepted event is at the head")
    void moveHistoryIsNewestFirstStack() {
        GameSession s = newGame();
        play(s, "e2e4 e7e5 g1f3");

        Deque<MoveEvent> history = s.gameState().movesHistory();
        assertEquals(3, history.size());
        assertSame(history.peekFirst(), s.gameState().lastMove(), "lastMove must return the stack top");
        assertEquals(new Tile("g1"), history.getFirst().from());
        assertEquals(new Tile("f3"), history.getFirst().to());
        assertEquals(new Tile("e2"), history.getLast().from(), "oldest event remains at the tail");
        assertEquals(new Tile("e4"), history.getLast().to());
    }

    @Test
    @DisplayName("An accepted capture updates the board and both captured-piece registry/history")
    void captureConservesPieceCounts() {
        GameSession s = newGame();
        play(s, "e2e4 d7d5 e4d5");
        assertPiece(s, "d5", PieceColor.WHITE, PieceType.PAWN);
        assertEquals(31, occupiedSquares(s.gameState().board()));
        assertEquals(1, s.gameState().eatenPieces().get(PieceColor.BLACK).size());
        assertEquals(PieceType.PAWN, s.gameState().movesHistory().getFirst().pieceCaptured().type());
    }

    @Test
    @DisplayName("Stored move event contains a reconstructible complete position after each ply")
    void historyContainsFullFenForEveryPosition() {
        GameSession s = newGame();
        play(s, "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6");
        Deque<MoveEvent> events = s.gameState().movesHistory();
        assertEquals(8, events.size());
        assertSame(events.getFirst(), s.gameState().lastMove(), "lastMove must read history head/top");
        for (MoveEvent event : events) {
            String stored = event.gameStateFen();
            assertEquals(6, stored.trim().split("\\s+").length,
                    "event must record the complete six-field position after " + event.from() + event.to());
            GameSession reconstructed = at(stored);
            assertEquals(stored, reconstructed.toFEN(), "stored position must be independently loadable");
        }
        assertEquals(s.toFEN(), events.getFirst().gameStateFen(), "top event must be the current position");
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
            ChessDebugCli.applyCoordinateMove(replay,
                    e.from().name().toLowerCase(Locale.ROOT) + e.to().name().toLowerCase(Locale.ROOT));
        }
        assertEquals(original.toFEN(), replay.toFEN());
        assertEquals(original.gameState().status(), replay.gameState().status());
        assertEquals(original.gameState().movesHistory().size(), replay.gameState().movesHistory().size());
    }

    @Test
    @DisplayName("Legal move enumeration must not alter king caches, board, clocks or history")
    void repeatedLegalMoveQueryIsPure() {
        GameSession s = newGame();
        String initialFen = s.toFEN();
        int initialCount = s.gameState().movesHistory().size();
        int pawnMoves = s.gameState().board().pieceAt(new Tile("e2")).timesMoved();
        for (int i = 0; i < 100; i++) {
            new GameUpdater(s).pieceMoves(new Tile("e2"));
            new GameUpdater(s).pieceMoves(new Tile("e1"));
            new GameUpdater(s).pieceMoves(new Tile("g1"));
        }
        assertEquals(initialFen, s.toFEN());
        assertEquals(initialCount, s.gameState().movesHistory().size());
        assertEquals(pawnMoves, s.gameState().board().pieceAt(new Tile("e2")).timesMoved());
        assertEquals(new Tile("e1"), s.gameState().board().kingTile(PieceColor.WHITE));
    }

    @Test
    @DisplayName("A failed capture is atomic: no partially changed board or metadata")
    void failedCaptureDoesNotCorruptSession() {
        GameSession s = newGame();
        play(s, "e2e4 d7d5");

        String before = s.toFEN();
        int count = s.gameState().movesHistory().size();
        try {
            ChessDebugCli.applyCoordinateMove(s, "e4d5");
            assertPiece(s, "d5", PieceColor.WHITE, PieceType.PAWN);
        } catch (RuntimeException e) {
            assertEquals(before, s.toFEN(), "failed update must be rolled back");
            assertEquals(count, s.gameState().movesHistory().size());
        }
    }

    @Test
    @DisplayName("Two game sessions must have isolated boards, histories and player turns")
    void concurrentSessionObjectsAreIsolated() {
        GameSession a = newGame();
        GameSession b = newGame();
        play(a, "e2e4 e7e5");
        play(b, "d2d4 d7d5");
        assertNotEquals(a.id(), b.id());
        assertNotEquals(a.toFEN(), b.toFEN());
        assertEquals(2, a.gameState().movesHistory().size());
        assertEquals(2, b.gameState().movesHistory().size());
        assertPiece(a, "e4", PieceColor.WHITE, PieceType.PAWN);
        assertPiece(b, "d4", PieceColor.WHITE, PieceType.PAWN);
        assertNull(b.gameState().board().pieceAt(new Tile("e4")));
    }

    @Test
    @DisplayName("Board.of parses the position as given and does not retain initial pieces")
    void productionBoardFenImportDoesNotOverlayStartingPosition() {
        Board loaded = Board.of("4k3/8/8/8/8/8/8/4K3");
        assertEquals("4k3/8/8/8/8/8/8/4K3", loaded.toFEN());
        assertEquals(2, occupiedSquares(loaded));
        assertEquals(new Tile("e1"), loaded.kingTile(PieceColor.WHITE));
        assertEquals(new Tile("e8"), loaded.kingTile(PieceColor.BLACK));
    }

    private static int occupiedSquares(Board board) {
        int count = 0;
        for (File f : File.values()) for (Rank r : Rank.values()) {
            if (board.pieceAt(new Tile(f, r)) != null) count++;
        }
        return count;
    }
}
