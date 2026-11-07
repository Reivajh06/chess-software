package reivajh06.archchess.architecture.domain.model.movement;

import reivajh06.archchess.architecture.domain.model.Piece;
import reivajh06.archchess.architecture.domain.model.Tile;

public record MoveEvent(Piece pieceMoved, Move move, Tile from, Tile to, Piece pieceCaptured, String gameStateFen) {
}
