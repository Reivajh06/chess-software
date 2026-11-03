package reivajh06.datalake.events;

import reivajh06.chess.model.Color;
import reivajh06.chess.model.PieceType;

public class PromotePieceEvent extends Event {

    private final PieceType promotedPiece;
    private final Color color;
    private final PieceType promotedTo;

    public PromotePieceEvent(String username, PieceType promotedPiece, Color color, PieceType promotedTo) {
        super(username);
        this.promotedPiece = promotedPiece;
        this.color = color;
        this.promotedTo = promotedTo;
    }
}
