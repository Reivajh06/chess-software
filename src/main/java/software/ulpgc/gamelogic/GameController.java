package software.ulpgc.gamelogic;

import software.ulpgc.model.*;
import software.ulpgc.model.movement.Move;
import software.ulpgc.model.movement.MoveData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameController {

	private final Board board;

	public GameController(Board board) {
		this.board = board;
	}

	public Piece movePiece(Move move, Tile from, Tile to) {
		return move.effect().apply(board, from, to);
	}

	public Piece getPieceIn(Tile tile) {
		return board.pieceAt(tile);
	}

	public void undoMove(MoveData moveData) {
		if(moveData.move().effect() != null) {
			moveData.move().effect().undo(moveData);
			return;
		}

		Tile from = moveData.from();
		Tile to = moveData.to();

		Piece movedPiece = board.removePieceIn(to);
		board.addPiece(from, movedPiece);
		board.addPiece(moveData.to(), moveData.pieceCaptured());
		movedPiece.unmoved();
	}

	public List<Tile> calculateMoveTiles(PieceColor color, Tile start, Move move) {
		List<Tile> tiles = new ArrayList<>();
		Tile t = start;
		int movementFactor = color == PieceColor.BLACK ? -1 : 1;

		for(int i = 0; i < move.repetitions(); i++) {
			int fileIndex = t.file().code + move.x() - File.A.code;
			int rankIndex = t.rank().number + move.y() * movementFactor - Rank.R1.number;

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

	public List<Tile> piecesTilesByColor(PieceColor color) {
		return board.asStream()
				.filter(t -> board.pieceAt(t) != null && board.pieceAt(t).color() == color)
				.toList();
	}

	public List<Tile> tilesByRank(Rank rank) {
		return board.asStream()
				.filter(t -> t.rank().equals(rank))
				.sorted(Comparator.comparing(Tile::file))
				.toList();
	}

	public void printBoard() {
		board.printBoard();
	}

	private boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}
}
