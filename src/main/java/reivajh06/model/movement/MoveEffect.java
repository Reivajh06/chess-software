package reivajh06.model.movement;

import reivajh06.model.Board;
import reivajh06.model.File;
import reivajh06.model.Piece;
import reivajh06.model.Tile;

public interface MoveEffect {

	MoveEffect DIRECT_CAPTURE = new MoveEffect() {

		@Override
		public Piece apply(Board board, Tile from, Tile to) {
			Piece movedPiece = board.removePieceIn(from);
			Piece capturedPiece = board.removePieceIn(to);

			board.addPiece(to, movedPiece);

			movedPiece.moved();

			return capturedPiece;
		}

		@Override
		public void undo(MoveEvent moveEvent, Board board) {
			Piece movedPiece = board.removePieceIn(moveEvent.to());

			board.addPiece(moveEvent.from(), movedPiece);

			if(moveEvent.pieceCaptured() != null) {
				board.addPiece(
						moveEvent.to(),
						moveEvent.pieceCaptured()
				);
			}

			movedPiece.unmoved();
		}
	};

	MoveEffect EN_PASSANT_CAPTURE = new MoveEffect() {
		@Override
		public Piece apply(Board board, Tile from, Tile to) {
			Tile adjacentTile = new Tile(to.file(), from.rank());
			Piece capturedPiece = board.pieceAt(adjacentTile);

			board.addPiece(to, board.removePieceIn(from));
			board.removePieceIn(adjacentTile);

			board.pieceAt(to).moved();
			return capturedPiece;
		}

		@Override
		public void undo(MoveEvent moveEvent, Board board) {
			Piece movedPawn = board.removePieceIn(moveEvent.to());

			board.addPiece(moveEvent.from(), movedPawn);

			Tile adjacentTile = new Tile(moveEvent.to().file(), moveEvent.from().rank());

			board.addPiece(adjacentTile, moveEvent.pieceCaptured());

			movedPawn.unmoved();
		}
	};

	MoveEffect QUEENSIDE_CASTLING = new MoveEffect() {
		@Override
		public Piece apply(Board board, Tile from, Tile to) {
			Piece king = board.removePieceIn(from);
			Piece rook = board.removePieceIn(new Tile(File.A, from.rank()));

			board.addPiece(to, king);
			board.addPiece(new Tile(File.D, from.rank()), rook);

			king.moved();
			rook.moved();

			return null;
		}

		@Override
		public void undo(MoveEvent moveEvent, Board board) {
			Tile rookCurrentTile = new Tile(
					File.D,
					moveEvent.from().rank()
			);

			Tile rookOriginalTile = new Tile(
					File.A,
					moveEvent.from().rank()
			);

			Piece king = board.removePieceIn(moveEvent.to());
			Piece rook = board.removePieceIn(rookCurrentTile);

			board.addPiece(
					moveEvent.from(),
					king
			);

			board.addPiece(
					rookOriginalTile,
					rook
			);

			king.unmoved();
			rook.unmoved();
		}
	};

	MoveEffect KINGSIDE_CASTLING = new MoveEffect() {
		@Override
		public Piece apply(Board board, Tile from, Tile to) {
			Piece king = board.removePieceIn(from);
			Piece rook = board.removePieceIn(new Tile(File.H, from.rank()));

			board.addPiece(to, king);
			board.addPiece(new Tile(File.F, from.rank()), rook);

			king.moved();
			rook.moved();

			return null;
		}

		@Override
		public void undo(MoveEvent moveEvent, Board board) {
			Tile rookCurrentTile = new Tile(
					File.F,
					moveEvent.from().rank()
			);

			Tile rookOriginalTile = new Tile(
					File.H,
					moveEvent.from().rank()
			);

			Piece king = board.removePieceIn(moveEvent.to());
			Piece rook = board.removePieceIn(rookCurrentTile);

			board.addPiece(
					moveEvent.from(),
					king
			);

			board.addPiece(
					rookOriginalTile,
					rook
			);

			king.unmoved();
			rook.unmoved();
		}
	};

	Piece apply(Board board, Tile from, Tile to);
	void undo(MoveEvent moveEvent, Board board);
}
