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
		public void undo(MoveData moveData) {
			Board board = moveData.board();

			Piece movedPiece = board.removePieceIn(moveData.to());

			board.addPiece(moveData.from(), movedPiece);

			if(moveData.pieceCaptured() != null) {
				board.addPiece(
						moveData.to(),
						moveData.pieceCaptured()
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
		public void undo(MoveData moveData) {
			Board board = moveData.board();

			Piece movedPawn = board.removePieceIn(moveData.to());

			board.addPiece(moveData.from(), movedPawn);

			Tile adjacentTile = new Tile(moveData.to().file(), moveData.from().rank());

			board.addPiece(adjacentTile, moveData.pieceCaptured());

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
		public void undo(MoveData moveData) {
			Board board = moveData.board();

			Tile rookCurrentTile = new Tile(
					File.D,
					moveData.from().rank()
			);

			Tile rookOriginalTile = new Tile(
					File.A,
					moveData.from().rank()
			);

			Piece king = board.removePieceIn(moveData.to());
			Piece rook = board.removePieceIn(rookCurrentTile);

			board.addPiece(
					moveData.from(),
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
		public void undo(MoveData moveData) {
			Board board = moveData.board();

			Tile rookCurrentTile = new Tile(
					File.F,
					moveData.from().rank()
			);

			Tile rookOriginalTile = new Tile(
					File.H,
					moveData.from().rank()
			);

			Piece king = board.removePieceIn(moveData.to());
			Piece rook = board.removePieceIn(rookCurrentTile);

			board.addPiece(
					moveData.from(),
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
	void undo(MoveData moveData);
}
