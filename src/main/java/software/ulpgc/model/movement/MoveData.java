package software.ulpgc.model.movement;

import software.ulpgc.model.Piece;
import software.ulpgc.model.Tile;

public record MoveData(Piece piece, Move move, Tile from, Tile to, Piece pieceEaten) {
}
