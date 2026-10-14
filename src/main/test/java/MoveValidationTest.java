import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reivajh06.gamelogic.Difficulty;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameUpdater;
import reivajh06.gamelogic.Gamemode;
import reivajh06.model.Tile;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveValidationTest {
    @ParameterizedTest(name = "Reject {0}{1} with rule {2}")
    @CsvSource({
            "e2, e5, 0",
            "e7, e6, 0",
            "a1, a3, 2",
            "a1, b1, 1"
    })
    void updaterRejectsIllegalMovesWithoutChangingState(String from, String to, int moveId) {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        assertRejectedWithoutMutation(session, from, to, moveId);
    }

    @Test
    void updaterRejectsMovesExposingOwnKing() {
        GameSession session = ChessTestSupport.sessionAt("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1");
        assertRejectedWithoutMutation(session, "e2", "d2", 0);
    }

    @Test
    void unknownRuleDoesNotChangeState() {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        assertRejectedWithoutMutation(session, "e2", "e4", 99);
    }

    @Test
    void emptyOriginDoesNotChangeState() {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        assertRejectedWithoutMutation(session, "e3", "e4", 0);
    }

    private static void assertRejectedWithoutMutation(GameSession session, String from, String to, int moveId) {
        String before = session.toFEN();
        int historySize = session.gameState().movesHistory().size();
        try {
            new GameUpdater(session).movePieceAndUpdate(moveId, new Tile(from), new Tile(to));
        } catch (RuntimeException rejected) {
            assertEquals(before, session.toFEN());
        }

        assertAll(
                () -> assertEquals(before, session.toFEN(), "an illegal request must not change the position"),
                () -> assertEquals(historySize, session.gameState().movesHistory().size(),
                        "an illegal request must not add a move event")
        );
    }
}
