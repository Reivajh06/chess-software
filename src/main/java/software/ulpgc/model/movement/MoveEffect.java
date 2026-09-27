package software.ulpgc.model.movement;

import software.ulpgc.model.Board;
import software.ulpgc.model.File;
import software.ulpgc.model.Piece;
import software.ulpgc.model.Tile;

public interface MoveEffect {

	MoveEffect DIRECT_CAPTURE = ((board, from, to) -> {
		Piece movedPiece = board.removePieceIn(from);
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

	MoveEffect QUEENSIDE_CASTLING = (board, from, to) -> {
		board.addPiece(to, board.removePieceIn(from));
		board.addPiece(
				new Tile(File.D, from.rank()),
				board.removePieceIn(new Tile(File.A, from.rank()))
		);

		return null;
	};

	MoveEffect KINGSIDE_CASTLING = (board, from, to) -> {
		board.addPiece(to, board.removePieceIn(from));
		board.addPiece(
				new Tile(File.F, from.rank()),
				board.removePieceIn(new Tile(File.H, from.rank()))
		);

		return null;
	};

	Piece apply(Board board, Tile from, Tile to);
}
