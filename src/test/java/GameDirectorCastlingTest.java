import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameDirector;
import reivajh06.model.Castling;
import reivajh06.model.Color;
import reivajh06.model.Tile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class GameDirectorCastlingTest {
    private static final String OPEN_ROOKS =
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1";

    private static GameSession at(String fen) {
        return ChessTestSupport.sessionAt(fen);
    }

    private static void play(GameSession session, String uci) {
        ChessTestSupport.playMove(session, uci);
    }

    private static String rights(GameSession session) {
        return session.toFEN().split("\\s+")[2];
    }

    private static boolean containsRight(GameSession session, Color color, Castling side) {
        return session.gameState().castlingRights().hasRight(color, side);
    }

    private static boolean canMove(GameSession session, String from, String to) {
        Map<Integer, List<Tile>> legal = new GameDirector(session).pieceMoves(new Tile(from));
        Tile destination = new Tile(to);
        return legal.values().stream().flatMap(Collection::stream).anyMatch(destination::equals);
    }

    private static Stream<Arguments> originalRookMoves() {
        return Stream.of(
                Arguments.of("white h-rook", OPEN_ROOKS, "h1h2", "Qkq", Color.WHITE, Castling.KINGSIDE),
                Arguments.of("white a-rook", OPEN_ROOKS, "a1a2", "Kkq", Color.WHITE, Castling.QUEENSIDE),
                Arguments.of("black h-rook", OPEN_ROOKS.replace(" w ", " b "), "h8h7", "KQq", Color.BLACK, Castling.KINGSIDE),
                Arguments.of("black a-rook", OPEN_ROOKS.replace(" w ", " b "), "a8a7", "KQk", Color.BLACK, Castling.QUEENSIDE)
        );
    }

    @ParameterizedTest(name = "{0} permanently loses only its own castling side")
    @MethodSource("originalRookMoves")
    void rookMovesRevokeExactlyOneRight(String label, String initial, String uci,
                                        String expectedRights, Color color, Castling revoked) {
        GameSession session = at(initial);
        assertTrue(containsRight(session, color, revoked), "fixture must initially allow this side");
        play(session, uci);
        assertEquals(expectedRights, rights(session), label);
        assertFalse(containsRight(session, color, revoked), "moved rook may no longer castle");
    }

    private static Stream<Arguments> kingMoves() {
        return Stream.of(
                Arguments.of("white king", OPEN_ROOKS, "e1e2", "kq", Color.WHITE),
                Arguments.of("black king", OPEN_ROOKS.replace(" w ", " b "), "e8e7", "KQ", Color.BLACK)
        );
    }

    @ParameterizedTest(name = "{0} revokes both sides")
    @MethodSource("kingMoves")
    void kingMoveRevokesBothRights(String label, String initial, String uci,
                                  String expectedRights, Color color) {
        GameSession session = at(initial);
        play(session, uci);
        assertEquals(expectedRights, rights(session), label);
        assertFalse(containsRight(session, color, Castling.KINGSIDE));
        assertFalse(containsRight(session, color, Castling.QUEENSIDE));
    }

    @Test
    @DisplayName("A non-king/non-rook move does not revoke either castling right")
    void ordinaryPieceMoveKeepsAllRights() {
        GameSession session = at("r3k2r/8/8/8/8/8/8/R3K1NR w KQkq - 0 1");
        play(session, "g1f3");
        assertEquals("KQkq", rights(session));
    }

    @Test
    @DisplayName("Capture of black a8 rook removes black queenside rights as well")
    void capturedCornerRookRevokesOpponentsRight() {
        GameSession session = at("r3k2r/8/8/8/8/8/6B1/R3K2R w KQkq - 0 1");
        play(session, "g2a8");
        assertEquals("KQk", rights(session));
        assertFalse(containsRight(session, Color.BLACK, Castling.QUEENSIDE));
        assertTrue(containsRight(session, Color.BLACK, Castling.KINGSIDE));
    }

    @Test
    @DisplayName("One corner-rook capture may revoke rights for both players")
    void cornerRookCapturingOpponentCornerRookRevokesBothSides() {
        GameSession session = at(OPEN_ROOKS);
        play(session, "a1a8");
        assertEquals("Kk", rights(session));
    }

    @Test
    @DisplayName("Historical castling rights cannot be reconstructed from unmoved-looking FEN pieces")
    void absentFenRightsProhibitCastlingEvenIfCornerRooksExist() {
        GameSession session = at("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1");
        assertEquals("-", rights(session));
        assertFalse(canMove(session, "e1", "g1"), "a rook and an unmoved king do not restore K");
        assertFalse(canMove(session, "e1", "c1"), "a rook and an unmoved king do not restore Q");
    }

    @Test
    @DisplayName("Revoked castling flags must not reappear after an unrelated move")
    void originallyAbsentRightsRemainAbsent() {
        GameSession session = at("r3k2r/8/8/8/8/8/P7/R3K2R w - - 0 1");
        play(session, "a2a3");
        assertEquals("-", rights(session));
    }

    @Test
    @DisplayName("A temporary obstruction or attacked transit square does not revoke history rights")
    void temporarilyUnavailableCastleKeepsTheRight() {
        GameSession session = at("r3k2r/8/8/8/8/5r2/P7/R3K2R w KQkq - 0 1");
        assertFalse(canMove(session, "e1", "g1"), "the black rook attacks f1");
        play(session, "a2a3");
        assertEquals("KQkq", rights(session));
    }

}
