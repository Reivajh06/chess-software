package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.MoveEvent;

import java.util.*;

public class GameStateBuilder {

    public static final List<Castling> CASTLING_ALL = List.of(Castling.KINGSIDE, Castling.QUEENSIDE);

    private Color turnColor;
    private Board board;
    private Deque<MoveEvent> movesHistory = new ArrayDeque<>();
    private Map<Color, List<Piece>> eatenPieces = new HashMap<>();
    private Color checkedColor;
    private GameStatus status;
    private CastlingRights castlingRights;
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

    public GameStateBuilder turnColor(Color turnColor) {
        this.turnColor = turnColor;
        return this;
    }

    public GameStateBuilder checkedColor(Color checkedColor) {
        this.checkedColor = checkedColor;
        return this;
    }

    public GameStateBuilder status(GameStatus status) {
        this.status = status;
        return this;
    }

    public GameStateBuilder eatenPieces(Map<Color, List<Piece>> eatenPieces) {
        this.eatenPieces = eatenPieces;
        return this;
    }

    public GameStateBuilder castlingRights(CastlingRights castlingRights) {
        this.castlingRights = castlingRights;
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
                castlingRights,
                enPassantDestination,
                halfMoveClock,
                fullMoves
        );
    }
}
