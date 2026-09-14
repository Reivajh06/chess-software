package reivajh06.model;

import reivajh06.model.pieces.Piece;

public record MoveData(Piece piece, Tile from, Tile to, Piece pieceEaten) {
}
