package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;

import java.util.ArrayList;
import java.util.List;

public class GameController {

	private static final int WHITE_PAWN_START_RANK = 2;
	private static final int WHITE_KING_START_RANK = 1;
	private static final int BLACK_PAWN_START_RANK = 7;
	private static final int BLACK_KING_START_RANK = 8;

	private final Board board;

	public GameController(Board board) {
		this.board = board;
	}

	public void setupBoard() {
		allocatePieces(board, PieceColor.BLACK);
		allocatePieces(board, PieceColor.WHITE);
	}

	private void allocatePieces(Board board, PieceColor color) {
		int pawnRank = WHITE_PAWN_START_RANK;
		int kingRank = WHITE_KING_START_RANK;

		if(color == PieceColor.BLACK) {
			pawnRank = BLACK_PAWN_START_RANK;
			kingRank = BLACK_KING_START_RANK;
		}

		for(int i = 0; i < 8; i++) {
			board.addPiece(new Tile(File.values()[i], Rank.values()[pawnRank - 1]), PieceFactory.createPawn(color));
		}

		board.addPiece(new Tile(File.A, Rank.values()[kingRank - 1]), PieceFactory.createRook(color));
		board.addPiece(new Tile(File.B, Rank.values()[kingRank - 1]), PieceFactory.createKnight(color));
		board.addPiece(new Tile(File.C, Rank.values()[kingRank - 1]), PieceFactory.createBishop(color));
		board.addPiece(new Tile(File.D, Rank.values()[kingRank - 1]), PieceFactory.createQueen(color));
		board.addPiece(new Tile(File.E, Rank.values()[kingRank - 1]), PieceFactory.createKing(color));
		board.addPiece(new Tile(File.F, Rank.values()[kingRank - 1]), PieceFactory.createBishop(color));
		board.addPiece(new Tile(File.G, Rank.values()[kingRank - 1]), PieceFactory.createKnight(color));
		board.addPiece(new Tile(File.H, Rank.values()[kingRank - 1]), PieceFactory.createRook(color));
	}

	public Piece movePiece(Move move, Tile from, Tile to) {
		return move.effect().apply(board, from, to);
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

			if(board.pieceAt(t) != null) break;
		}

		return tiles;
	}

	public List<Tile> getPiecesTilesByColor(PieceColor color) {
		return board.asStream()
				.filter(t -> board.pieceAt(t) != null && board.pieceAt(t).color() == color)
				.toList();
	}

	private boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}
}
