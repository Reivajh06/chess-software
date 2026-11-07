import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reivajh06.archchess.architecture.domain.model.game.GameConditions;
import reivajh06.archchess.architecture.domain.logic.GameController;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.architecture.domain.logic.GameDirector;
import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.Tile;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChessRulesTest {
    @ParameterizedTest(name = "{1}{2} legal = {3}")
    @CsvSource({
            "4k3/8/8/8/8/8/3r4/4K3 w - - 0 1, e1, d1, false",
            "4r1k1/8/8/8/8/8/8/4K3 w - - 0 1, e1, f1, true",
            "8/8/8/8/8/4k3/8/4K3 w - - 0 1, e1, e2, false",
            "4k3/8/8/8/8/8/3p4/4K3 w - - 0 1, e1, e2, true",
            "4k3/8/8/8/8/8/2p5/4K3 w - - 0 1, e1, d1, false"
    })
    void kingSafetyUsesItsDestination(String fen, String from, String to, boolean expected) {
        assertEquals(expected, canMove(ChessTestSupport.sessionAt(fen), from, to));
    }

    @Test
    void aPieceCannotCaptureTheOpposingKing() {
        GameSession session = ChessTestSupport.sessionAt("4k3/8/8/8/8/8/4R3/4K3 b - - 0 1");
        boolean kingCapture = GameController.legalPieceMoves(session.gameState(), new Tile("e2"))
                .values().stream().flatMap(Collection::stream).anyMatch(new Tile("e8")::equals);
        assertFalse(kingCapture);
    }

    @Test
    void queensideCastlingAllowsAnAttackOnTheRookPath() {
        GameSession session = ChessTestSupport.sessionAt("1r5k/8/8/8/8/8/8/R3K3 w Q - 0 1");
        assertTrue(canMove(session, "e1", "c1"), "the king never crosses b1");
    }

    @Test
    void castlingSafetyDoesNotTreatAPawnForwardMoveAsAnAttack() {
        GameSession session = ChessTestSupport.sessionAt("4k3/8/8/8/8/5p2/8/4K2R w K - 0 1");
        assertTrue(canMove(session, "e1", "g1"), "the pawn on f3 does not attack f1");
    }

    @ParameterizedTest(name = "Pawn on {1} cannot advance two ranks")
    @CsvSource({
            "4k3/8/8/8/4P3/8/8/4K3 w - - 0 1, e4, e6",
            "4k3/8/8/4p3/8/8/8/4K3 b - - 0 1, e5, e3"
    })
    void pawnDoublePushRequiresItsStartingRank(String fen, String from, String to) {
        assertFalse(canMove(ChessTestSupport.sessionAt(fen), from, to));
    }

    @Test
    void enPassantTargetExpiresAfterAnotherMove() {
        GameSession session = ChessTestSupport.sessionAt("4k3/3p4/8/4P3/8/8/8/4K1N1 b - - 0 1");
        ChessTestSupport.playMove(session, "d7d5");
        ChessTestSupport.playMove(session, "g1f3");
        assertNull(session.gameState().enPassantDestination());
    }

    @ParameterizedTest(name = "Dead position = {1} for {0}")
    @CsvSource({
            "7k/8/8/8/8/8/b7/2B1K3 w - - 0 1, false",
            "7k/8/8/8/8/b7/8/2B1K3 w - - 0 1, true"
    })
    void bishopDeadPositionsDependOnSquareColors(String fen, boolean expected) {
        GameSession session = ChessTestSupport.sessionAt(fen);
        assertEquals(expected, GameConditions.onDeadPosition(session.gameState()));
    }

    @Test
    void aStalematedKingHasNoLegalEscapeSquares() {
        GameSession session = ChessTestSupport.sessionAt("k7/2Q5/2K5/8/8/8/8/8 w - - 0 1");
        ChessTestSupport.playMove(session, "c7b6");
        assertAll(
                () -> assertEquals(Color.BLACK, session.gameState().turnColor()),
                () -> assertTrue(GameConditions.onStalemate(session.gameState())),
                () -> assertTrue(GameConditions.noLegalMoves(session.gameState(), Color.BLACK))
        );
    }

    @Test
    void enumeratingKingMovesPreservesThePositionAndMoveCounters() {
        GameSession session = ChessTestSupport.sessionAt("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        String before = session.toFEN();
        GameController.legalPieceMoves(session.gameState(), new Tile("e1"));

        assertAll(
                () -> assertEquals(before, session.toFEN()),
                () -> assertEquals(new Tile("e1"), session.gameState().board().kingTile(Color.WHITE)),
                () -> assertEquals(0, session.gameState().board().pieceAt(new Tile("e1")).timesMoved()),
                () -> assertEquals(0, session.gameState().board().pieceAt(new Tile("a1")).timesMoved()),
                () -> assertEquals(0, session.gameState().board().pieceAt(new Tile("h1")).timesMoved()),
                () -> assertTrue(session.gameState().movesHistory().isEmpty())
        );
    }

    private static boolean canMove(GameSession session, String from, String to) {
        return new GameDirector(session).pieceMoves(new Tile(from)).values().stream()
                .flatMap(Collection::stream)
                .anyMatch(new Tile(to)::equals);
    }
}
