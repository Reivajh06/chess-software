package reivajh06.datalake.events;

import reivajh06.chess.gamelogic.GameStatus;
import reivajh06.chess.model.PieceType;
import reivajh06.chess.model.Tile;

import java.util.UUID;

public class MovePieceEvent extends Event {

    private final String sessionId;
    private final Tile from;
    private final Tile to;
    private final PieceType pieceType;
    private final String stateFen;
    private final GameStatus status;

    public MovePieceEvent(String username, UUID sessionId, Tile from, Tile to, PieceType pieceType, String stateFen, GameStatus status) {
        super(username);
        this.sessionId = sessionId.toString();
        this.from = from;
        this.to = to;
        this.pieceType = pieceType;
        this.stateFen = stateFen;
        this.status = status;
    }

    public String sessionId() {
        return sessionId;
    }

    public Tile from() {
        return from;
    }

    public Tile to() {
        return to;
    }

    public PieceType pieceType() {
        return pieceType;
    }

    public String stateFen() {
        return stateFen;
    }

    public GameStatus status() {
        return status;
    }
}
