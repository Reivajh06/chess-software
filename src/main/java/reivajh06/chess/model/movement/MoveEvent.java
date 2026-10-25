package reivajh06.chess.model.movement;

import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;

public record MoveEvent(Piece pieceMoved, Move move, Tile from, Tile to, Piece pieceCaptured, String gameStateFen) {
}
