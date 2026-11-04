package reivajh06.datalake.events;

import reivajh06.chess.gamelogic.GameStatus;
import reivajh06.chess.model.Color;
import reivajh06.chess.model.PieceType;

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
}
