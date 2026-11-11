import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reivajh06.archchess.architecture.domain.model.game.Difficulty;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.architecture.domain.logic.GameDirector;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;
import reivajh06.archchess.architecture.domain.model.Piece;
import reivajh06.archchess.architecture.domain.model.Tile;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MoveValidationTest {
    private static final String PINNED_ROOK = "4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1";

    @ParameterizedTest(name = "{0}{1} is not offered as a legal destination")
    @CsvSource({
            "e2, e5",
            "a1, a3",
            "a1, b1"
    })
    void legalMovesExcludeIllegalDestinations(String from, String to) {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_PLUGIN);
        assertFalse(isOffered(session, from, to));
    }

    @Test
    void legalMovesAreEmptyForTheSideNotToMove() {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_PLUGIN);
        assertFalse(isOffered(session, "e7", "e6"));
    }

    @Test
    void legalMovesExcludeExposingOwnKing() {
        assertFalse(isOffered(ChessTestSupport.sessionAt(PINNED_ROOK), "e2", "d2"));
    }

    @ParameterizedTest(name = "Debug move {0}{1} with rule {2} is applied")
    @CsvSource({
            "e2, e5, 0",
            "e7, e6, 0",
            "a1, a3, 2",
            "a1, b1, 1"
    })
    void updaterAppliesUnvalidatedMovesForDebugging(String from, String to, int moveId) {
        assertApplied(new GameSession(Gamemode.PVP, Difficulty.NOT_PLUGIN), from, to, moveId);
    }

    @Test
    void updaterAppliesAMoveExposingOwnKingForDebugging() {
        assertApplied(ChessTestSupport.sessionAt(PINNED_ROOK), "e2", "d2", 0);
    }

    @Test
    void unknownRuleDoesNotChangeState() {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_PLUGIN);
        assertUnchanged(session, "e2", "e4", 99);
    }

    @Test
    void emptyOriginDoesNotChangeState() {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_PLUGIN);
        assertUnchanged(session, "e3", "e4", 0);
    }

    private static boolean isOffered(GameSession session, String from, String to) {
        return new GameDirector(session).pieceMoves(new Tile(from)).values().stream()
                .flatMap(Collection::stream)
                .anyMatch(new Tile(to)::equals);
    }

    private static void assertApplied(GameSession session, String from, String to, int moveId) {
        Piece moved = session.gameState().board().pieceAt(new Tile(from));
        int historySize = session.gameState().movesHistory().size();

        new GameDirector(session).movePieceAndUpdate(moveId, new Tile(from), new Tile(to));

        // El estado nuevo trabaja sobre una copia del tablero: se comprueba la pieza (tipo y color), no la instancia.
        Piece arrived = session.gameState().board().pieceAt(new Tile(to));
        assertAll(
                () -> assertNotNull(arrived, "piece reaches its destination"),
                () -> assertEquals(moved.type(), arrived.type(), "piece reaches its destination"),
                () -> assertEquals(moved.color(), arrived.color(), "piece reaches its destination"),
                () -> assertNull(session.gameState().board().pieceAt(new Tile(from)), "origin is vacated"),
                () -> assertEquals(historySize + 1, session.gameState().movesHistory().size(), "one event is recorded")
        );
    }

    private static void assertUnchanged(GameSession session, String from, String to, int moveId) {
        String before = session.toFEN();
        int historySize = session.gameState().movesHistory().size();
        try {
            new GameDirector(session).movePieceAndUpdate(moveId, new Tile(from), new Tile(to));
        } catch (RuntimeException rejected) {
            // The rejection mechanism is not part of the contract; only the unchanged state is.
        }

        assertAll(
                () -> assertEquals(before, session.toFEN(), "the position must not change"),
                () -> assertEquals(historySize, session.gameState().movesHistory().size(), "no move event is added")
        );
    }
}
