package reivajh06.gamelogic;

import reivajh06.model.*;
import software.ulpgc.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameController {

	public static Piece movePiece(Board board, Move move, Tile from, Tile to) {
		return move.effect().apply(board, from, to);
	}

	public static Piece getPieceIn(Board board, Tile tile) {
		return board.pieceAt(tile);
	}

	public static void undoMove(Board board, MoveData moveData) {
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

	public static List<Tile> calculateMoveTiles(Board board, PieceColor color, Tile start, Move move) {
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

	public static List<Tile> piecesTilesByColor(Board board, PieceColor color) {
		return board.asStream()
				.filter(t -> board.pieceAt(t) != null && board.pieceAt(t).color() == color)
				.toList();
	}

	public static List<Tile> tilesByRank(Board board, Rank rank) {
		return board.asStream()
				.filter(t -> t.rank().equals(rank))
				.sorted(Comparator.comparing(Tile::file))
				.toList();
	}

	private static boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}
}
