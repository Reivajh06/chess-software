package reivajh06.model.movement;

import reivajh06.model.Board;
import reivajh06.model.Piece;
import reivajh06.model.Tile;
//TODO change Board into String gameSnapshot
public record MoveEvent(Piece pieceMoved, Move move, Tile from, Tile to, Piece pieceCaptured, Board board) {
}
