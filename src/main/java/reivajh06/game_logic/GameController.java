package reivajh06.game_logic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;

import java.util.ArrayList;
import java.util.List;

public class GameController {

	private final Board board;

	public GameController(Board board) {
		this.board = board;
	}

	public void setupBoard() {
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

		for(int i = 0; i < 8; i++) {
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

	public Piece movePiece(Tile from, Tile to) {
		if(!board.hasPiece(from)) return null;

		Piece eaten = board.removeFromTile(to);
		board.addPiece(to, board.removeFromTile(from));

		return eaten;
	}

	public List<Tile> calculateMoveTiles(PieceColor color, Tile start, Move move) {
		List<Tile> tiles = new ArrayList<>();
		Tile t = start;
		int movementFactor = color == PieceColor.BLACK ? -1 : 1;

		for(int i = 0; i < move.repetitions(); i++) {
			int fileIndex = t.file().code + move.x() * movementFactor - File.A.code;
			int rankIndex = t.rank().number + move.y() * movementFactor - Rank.ONE.number;

			if(outOfBounds(fileIndex, rankIndex)) break;

			t = new Tile(
					File.values()[fileIndex],
					Rank.values()[rankIndex]
			);

			tiles.add(t);
		}

		return tiles;
	}

	private boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}

	private boolean isDiagonalMove(Tile from, Tile to) {
		int xOffset = Math.abs(from.rank().number - to.rank().number);
		int yOffset = Math.abs(from.file().code - to.file().code);

		return (xOffset == 1) && (yOffset == 1);
	}

	private boolean validTile(Tile t) {
		return t.file().code <= File.H.code &&
				t.file().code >= File.A.code &&
				t.rank().number <= 8 &&
				t.rank().number > 0;
	}
}
