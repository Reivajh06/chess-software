package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.MoveEvent;

import java.util.*;

public class GameStateBuilder {

    public static final List<Castling> CASTLING_ALL = List.of(Castling.KINGSIDE, Castling.QUEENSIDE);

    private PieceColor turnColor;
    private Board board;
    private Deque<MoveEvent> movesHistory = new ArrayDeque<>();
    private Map<PieceColor, List<Piece>> eatenPieces = new HashMap<>();
    private PieceColor checkedColor;
    private GameStatus status;
    private List<Castling> whiteCastling;
    private List<Castling> blackCastling;
    private Tile enPassantDestination;
    private int halfMoveClock;
    private int fullMoves = 0;

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

    public GameStateBuilder eatenPieces(Map<PieceColor, List<Piece>> eatenPieces) {
        this.eatenPieces = eatenPieces;
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

    public GameStateBuilder halfMoveClock(int movesAfterPawn) {
        this.halfMoveClock = movesAfterPawn;
        return this;
    }

    public GameStateBuilder fullMoves(int fullMoves) {
        this.fullMoves = fullMoves;
        return this;
    }

    public GameState build() {
        return new GameState(
                board,
                movesHistory,
                eatenPieces,
                turnColor,
                checkedColor,
                status,
                whiteCastling,
                blackCastling,
                enPassantDestination,
                halfMoveClock,
                fullMoves
        );
    }
}
