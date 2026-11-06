package reivajh06.infrastructure.adapters.datalake.events;

import reivajh06.architecture.domain.model.game.GameStatus;
import reivajh06.architecture.domain.model.Color;
import reivajh06.architecture.domain.model.PieceType;

import java.util.UUID;

public class PromotePieceEvent extends Event {

    private final PieceType promotedPiece;
    private final Color color;
    private final PieceType promotedTo;
    private final String sessionId;
    private final GameStatus status;
    private final String stateFen;

    public PromotePieceEvent(String username, PieceType promotedPiece, Color color, PieceType promotedTo, UUID sessionId, GameStatus status, String stateFen) {
        super(username);
        this.promotedPiece = promotedPiece;
        this.color = color;
        this.promotedTo = promotedTo;
        this.sessionId = sessionId.toString();
        this.status = status;
        this.stateFen = stateFen;
    }

    public PieceType promotedPiece() {
        return promotedPiece;
    }

    public Color color() {
        return color;
    }

    public PieceType promotedTo() {
        return promotedTo;
    }

    public String sessionId() {
        return sessionId;
    }

    public GameStatus status() {
        return status;
    }

    public String stateFen() {
        return stateFen;
    }
}
