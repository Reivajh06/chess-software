package reivajh06.archchess.infrastructure.adapters.datalake.events;

import reivajh06.archchess.architecture.domain.model.PieceType;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.game.GameStatus;

import java.util.UUID;

public class MovePieceEvent extends Event {

    private final String sessionId;
    private final int moveId;
    private final Tile from;
    private final Tile to;
    private final PieceType pieceType;
    private final String stateFen;
    private final GameStatus status;

    public MovePieceEvent(String username, UUID sessionId, int moveId, Tile from, Tile to, PieceType pieceType, String stateFen, GameStatus status) {
        super(username);
        this.sessionId = sessionId.toString();
        this.moveId = moveId;
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
