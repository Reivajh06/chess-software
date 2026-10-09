package reivajh06.model.movement;

import reivajh06.model.Piece;
import reivajh06.model.Tile;

public record MoveEvent(Piece pieceMoved, Move move, Tile from, Tile to, Piece pieceCaptured, String gameStateFen) {
}
