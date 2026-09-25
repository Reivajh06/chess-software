package reivajh06.model.movement;

import reivajh06.model.Board;
import reivajh06.model.Piece;
import reivajh06.model.Tile;

public interface MoveEffect {

	MoveEffect DIRECT_CAPTURE = ((board, from, to) -> {
		Piece movedPiece = board.removePieceIn(from);
		movedPiece.moved();
		Piece capturedPiece = board.removePieceIn(to);
		board.addPiece(to, movedPiece);

		return capturedPiece;
	});

	MoveEffect PAWN_DIAGONAL_CAPTURE = (board, from, to) -> {
		if(board.pieceAt(to) != null) return MoveEffect.DIRECT_CAPTURE.apply(board, from, to);

		Tile adjacentTile = new Tile(to.file(), from.rank());
		Piece capturedPiece = board.pieceAt(adjacentTile);

		board.addPiece(to, board.removePieceIn(from));
		board.removePieceIn(adjacentTile);
		return capturedPiece;
	};

	Piece apply(Board board, Tile from, Tile to);
}
