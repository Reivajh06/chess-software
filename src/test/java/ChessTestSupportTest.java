import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Color;
import reivajh06.chess.model.Tile;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChessTestSupportTest {
    @Test
    void fixturePreservesFenFieldsAndInitializesKingLocations() {
        String fen = "r3k2r/8/8/8/8/8/8/R3K2R b Qk - 17 34";
        GameSession session = ChessTestSupport.sessionAt(fen);

        assertAll(
                () -> assertEquals(fen, session.toFEN()),
                () -> assertEquals(new Tile("e1"), session.gameState().board().kingTile(Color.WHITE)),
                () -> assertEquals(new Tile("e8"), session.gameState().board().kingTile(Color.BLACK)),
                () -> assertEquals(0, session.gameState().movesHistory().size())
        );
    }

    @Test
    void fixtureInitializesCheckForTheActiveKing() {
        GameSession session = ChessTestSupport.sessionAt("4r1k1/8/8/8/8/8/8/4K2R w K - 0 1");
        assertEquals(Color.WHITE, session.gameState().checkedColor());
    }

    @Test
    void coordinateDriverResolvesTheActualMoveIndex() {
        GameSession session = ChessTestSupport.sessionAt("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        ChessTestSupport.playMove(session, "e2e4");

        assertAll(
                () -> assertEquals(new Tile("e4"), session.gameState().lastMove().to()),
                () -> assertEquals(2, session.gameState().lastMove().move().y()),
                () -> assertEquals(1, session.gameState().movesHistory().size())
        );
    }

    @Test
    void unsupportedPromotionSuffixLeavesTheFixtureUnchanged() {
        String fen = "7k/P7/6K1/8/8/8/8/8 w - - 0 1";
        GameSession session = ChessTestSupport.sessionAt(fen);

        assertThrows(UnsupportedOperationException.class, () -> ChessTestSupport.playMove(session, "a7a8q"));
        assertEquals(fen, session.toFEN());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4k3/8/8/8/8/8/8/4K3 w - - 0",
            "4k3/8/8/8/8/8/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/8/3K3 w - - 0 1",
            "4k3/8/8/8/8/8/8/4K3 w - e4 0 1",
            "4k3/8/8/8/8/8/8/8 w - - 0 1"
    })
    void fixtureRejectsMalformedFen(String fen) {
        assertThrows(IllegalArgumentException.class, () -> ChessTestSupport.sessionAt(fen));
    }
}
