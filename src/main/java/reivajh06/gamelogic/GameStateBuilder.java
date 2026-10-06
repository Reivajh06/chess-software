package reivajh06.gamelogic;

import reivajh06.model.Board;
import reivajh06.model.Castling;
import reivajh06.model.PieceColor;
import reivajh06.model.Tile;
import reivajh06.model.movement.MoveEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class GameStateBuilder {

    public static final List<Castling> CASTLING_ALL = List.of(Castling.KINGSIDE, Castling.QUEENSIDE);

    private PieceColor turnColor;
    private Board board;
    private Deque<MoveEvent> movesHistory = new ArrayDeque<>();
    private PieceColor checkedColor;
    private GameStatus status;
    private List<Castling> whiteCastling;
    private List<Castling> blackCastling;
    private Tile enPassantDestination;
    private int movesAfterPawn;
    private int nTotalPlay = 0;

    public GameStateBuilder board(Board board) {
        this.board = board;
        return this;
    }

    public GameStateBuilder movesHistory(Deque<MoveEvent> movesHistory) {
        this.movesHistory = movesHistory;
        return this;
    }

    public GameStateBuilder turnColor(PieceColor turnColor) {
        this.turnColor = turnColor;
        return this;
    }

    public GameStateBuilder checkedColor(PieceColor checkedColor) {
        this.checkedColor = checkedColor;
        return this;
    }

    public GameStateBuilder status(GameStatus status) {
        this.status = status;
        return this;
    }

    public GameStateBuilder whiteCastling(List<Castling> whiteCastling) {
        this.whiteCastling = whiteCastling;
        return this;
    }

    public GameStateBuilder blackCastling(List<Castling> blackCastling) {
        this.blackCastling = blackCastling;
        return this;
    }

    public GameStateBuilder enPassantDestination(Tile enPassantDestination) {
        this.enPassantDestination = enPassantDestination;
        return this;
    }

    public GameStateBuilder movesAfterPawn(int movesAfterPawn) {
        this.movesAfterPawn = movesAfterPawn;
        return this;
    }

    public GameStateBuilder nTotalPlay(int nTotalPlay) {
        this.nTotalPlay = nTotalPlay;
        return this;
    }

    public GameState build() {
        return new GameState(
                board,
                movesHistory,
                turnColor,
                checkedColor,
                status,
                whiteCastling,
                blackCastling,
                enPassantDestination,
                movesAfterPawn,
                nTotalPlay
        );
    }
}
