package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveData;

import java.util.*;
import java.util.stream.Collectors;

public class GameController {

	public static Piece movePiece(GameState gameState, Move move, Tile from, Tile to) {
		return move.effect().apply(gameState.board(), from, to);
	}

	public static void undoMove(GameState gameState, MoveData moveData) {
		if(moveData.move().effect() != null) {
			moveData.move().effect().undo(moveData);
			return;
		}

		Tile from = moveData.from();
		Tile to = moveData.to();

		Piece movedPiece = gameState.board().removePieceIn(to);
		gameState.board().addPiece(from, movedPiece);
		gameState.board().addPiece(moveData.to(), moveData.pieceCaptured());
		movedPiece.unmoved();
	}

	public static List<Tile> calculateMoveTiles(GameState gameState, PieceColor color, Tile start, Move move) {
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

			if(gameState.board().pieceAt(t) != null) break;
		}

		return tiles;
	}

	public static List<Tile> piecesTilesByColor(GameState gameState, PieceColor color) {
		return gameState.board().asStream()
				.filter(t -> gameState.board().pieceAt(t) != null && gameState.board().pieceAt(t).color() == color)
				.toList();
	}

	public static List<Tile> tilesByRank(GameState gameState, Rank rank) {
		return gameState.board().asStream()
				.filter(t -> t.rank().equals(rank))
				.sorted(Comparator.comparing(Tile::file))
				.toList();
	}

	public static Map<Move, List<Tile>> legalPieceMoves(GameState gameState, Tile location) {
		if(gameState.board().pieceAt(location) == null) return new HashMap<>();

		Map<Move, List<Tile>> movesetMap = validPieceMoves(gameState, location);

		Map<Move, List<Tile>> legalMoveset = new HashMap<>();

		for(Map.Entry<Move, List<Tile>> moveEntry : movesetMap.entrySet()) {
			Move move = moveEntry.getKey();
			List<Tile> moveTiles = new ArrayList<>(moveEntry.getValue());

			moveTiles.removeIf(tile -> !GameConditions.moveUnchecksKing(gameState, move, location, tile));

			legalMoveset.put(move, moveTiles);
		}

		return legalMoveset;
	}

	public static Map<Move, List<Tile>> pieceMovesetTiles(GameState gameState, Tile location) {
		if(gameState.board().pieceAt(location) == null) return new HashMap<>();

		Piece piece = gameState.board().pieceAt(location);

		List<Move> pieceMoveset = piece.moveset();

		Map<Move, List<Tile>> movesetTiles = new HashMap<>();

		for(Move move : pieceMoveset) {
			List<Tile> filteredMoveTiles = calculateMoveTiles(
					gameState,
					piece.color(),
					location,
					move
			);

			movesetTiles.put(move, filteredMoveTiles);
		}

		return movesetTiles;
	}

	public static Map<Move, List<Tile>> validPieceMoves(GameState gameState, Tile location) {
		if(gameState.board().pieceAt(location) == null) return new HashMap<>();
		if(gameState.status() != GameStatus.ACTIVE) return new HashMap<>();

		return pieceMovesetTiles(gameState, location).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> {
					Move move = e.getKey();
					List<Tile> pendingTiles = e.getValue();

					return MovesValidator.filterMoveTiles(
							gameState,
							move,
							location,
							pendingTiles,
							gameState.lastMove()
					);
				}));
	}

	private static boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}
}
