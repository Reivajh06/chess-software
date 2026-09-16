package reivajh06.model;

public class GameController {

	public void setupBoard(Board board) {
		allocatePieces(board, PieceColor.BLACK);
		allocatePieces(board, PieceColor.WHITE);
	}

	private void allocatePieces(Board board, PieceColor color) {
		int pawnRow = 2;
		int kingRow = 1;

		if(color == PieceColor.BLACK) {
			pawnRow = 7;
			kingRow = 8;
		}

		for(int i = 0; i < 8; i++) {						//Unicode based
			board.addPiece(new Tile(File.values()[i], Rank.values()[pawnRow - 1]), new Piece(PieceType.PAWN, color));
		}

		board.addPiece(new Tile(File.A, Rank.values()[kingRow - 1]), new Piece(PieceType.ROOK, color));
		board.addPiece(new Tile(File.B, Rank.values()[kingRow - 1]), new Piece(PieceType.KNIGHT, color));
		board.addPiece(new Tile(File.C, Rank.values()[kingRow - 1]), new Piece(PieceType.BISHOP, color));
		board.addPiece(new Tile(File.D, Rank.values()[kingRow - 1]), new Piece(PieceType.QUEEN, color));
		board.addPiece(new Tile(File.E, Rank.values()[kingRow - 1]), new Piece(PieceType.KING, color));
		board.addPiece(new Tile(File.F, Rank.values()[kingRow - 1]), new Piece(PieceType.BISHOP, color));
		board.addPiece(new Tile(File.G, Rank.values()[kingRow - 1]), new Piece(PieceType.KNIGHT, color));
		board.addPiece(new Tile(File.H, Rank.values()[kingRow - 1]), new Piece(PieceType.ROOK, color));
	}
}
