package reivajh06.gamelogic;

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
			board.addPiece(new Tile(File.values()[i], Rank.values()[pawnRow - 1]), PieceFactory.createPawn(color));
		}

		board.addPiece(new Tile(File.A, Rank.values()[kingRow - 1]), PieceFactory.createRook(color));
		board.addPiece(new Tile(File.B, Rank.values()[kingRow - 1]), PieceFactory.createKnight(color));
		board.addPiece(new Tile(File.C, Rank.values()[kingRow - 1]), PieceFactory.createBishop(color));
		board.addPiece(new Tile(File.D, Rank.values()[kingRow - 1]), PieceFactory.createQueen(color));
		board.addPiece(new Tile(File.E, Rank.values()[kingRow - 1]), PieceFactory.createKing(color));
		board.addPiece(new Tile(File.F, Rank.values()[kingRow - 1]), PieceFactory.createBishop(color));
		board.addPiece(new Tile(File.G, Rank.values()[kingRow - 1]), PieceFactory.createKnight(color));
		board.addPiece(new Tile(File.H, Rank.values()[kingRow - 1]), PieceFactory.createRook(color));
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

	private boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}
}
