package software.ulpgc.model.movement;

import software.ulpgc.model.Board;
import software.ulpgc.model.Piece;
import software.ulpgc.model.Tile;

public record MoveData(Piece pieceMoved, Move move, Tile from, Tile to, Piece pieceCaptured, Board board) {
}
