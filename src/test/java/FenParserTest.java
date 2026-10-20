import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reivajh06.FenParser;
import reivajh06.gamelogic.Difficulty;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.Gamemode;
import reivajh06.model.Board;
import reivajh06.model.Color;
import reivajh06.model.PieceType;
import reivajh06.model.Tile;

import static org.junit.jupiter.api.Assertions.*;

class FenParserTest {
    private static final String START =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Test
    @DisplayName("A new game serializes to standard six-field FEN")
    void startingFen() {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        assertEquals(START, session.toFEN());
    }

    @Test
    @DisplayName("A board-only FEN round trip reproduces piece placement")
    void boardPlacementRoundTrip() {
        String position = "8/8/8/3k4/8/4K3/8/8";
        Board board = FenParser.fenToBoard(position);
        assertEquals(position, FenParser.boardToFen(board));
        assertEquals(PieceType.KING, board.pieceAt(new Tile("d5")).type());
        assertEquals(PieceType.KING, board.pieceAt(new Tile("e3")).type());
        assertNull(board.pieceAt(new Tile("e1")));
    }

    @Test
    @DisplayName("Board FEN import must initialize derived king-square tracking")
    void boardImportInitializesKingLocations() {
        Board board = FenParser.fenToBoard("4k3/8/8/8/8/8/8/4K3");
        assertAll(
                () -> assertEquals(new Tile("e1"), board.kingTile(Color.WHITE)),
                () -> assertEquals(new Tile("e8"), board.kingTile(Color.BLACK))
        );
    }

    @Test
    @DisplayName("Board copy preserves king locations and movement counters, not only FEN placement")
    void boardCopyPreservesNonFenState() {
        Board board = Board.startingBoard();
        board.pieceAt(new Tile("h1")).moved();
        Board copy = board.copy();
        assertNotSame(board, copy);
        assertEquals(FenParser.boardToFen(board), FenParser.boardToFen(copy));
        assertAll(
                () -> assertEquals(new Tile("e1"), copy.kingTile(Color.WHITE)),
                () -> assertEquals(new Tile("e8"), copy.kingTile(Color.BLACK)),
                () -> assertEquals(1, copy.pieceAt(new Tile("h1")).timesMoved()),
                () -> assertNotSame(board.pieceAt(new Tile("h1")), copy.pieceAt(new Tile("h1")))
        );
    }

    @Test
    @DisplayName("Imported full FEN preserves explicit historical castling flags and clocks")
    void sixFieldsRemainExact() {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        String fen = "r3k2r/8/8/8/8/8/8/R3K2R w Qk - 17 34";
        ChessTestSupport.loadFen(session, fen);
        assertEquals(fen, session.toFEN());
    }
}
