package reivajh06.datalake.events;

import reivajh06.chess.model.PieceType;
import reivajh06.chess.model.Tile;

import java.util.UUID;

public class MovePieceEvent extends Event {

    private final String sessionId;
    private final Tile from;
    private final Tile to;
    private final PieceType pieceType;
    private final String gamestateFen;

    public MovePieceEvent(String username, UUID sessionId, Tile from, Tile to, PieceType pieceType, String gamestateFen) {
        super(username);
        this.sessionId = sessionId.toString();
        this.from = from;
        this.to = to;
        this.pieceType = pieceType;
        this.gamestateFen = gamestateFen;
    }
}
