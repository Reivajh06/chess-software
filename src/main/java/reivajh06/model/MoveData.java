package reivajh06.model;

import reivajh06.model.pieces.Piece;

public record MoveData(Piece piece, String from, String to, Piece pieceEaten) {
}
