package reivajh06.chess.gamelogic;

import reivajh06.chess.model.*;
import reivajh06.chess.model.movement.Move;
import reivajh06.chess.model.movement.MoveEvent;

import java.util.*;
import java.util.stream.Collectors;

public class GameController {

	public static Piece movePiece(GameState gameState, Move move, Tile from, Tile to) {
		return move.effect().apply(gameState.board(), from, to);
	}

	public static void undoMove(GameState gameState, MoveEvent moveEvent) {
		if(moveEvent.move().effect() != null) {
			moveEvent.move().effect().undo(moveEvent, gameState.board());
			return;
		}

		Tile from = moveEvent.from();
		Tile to = moveEvent.to();

		Piece movedPiece = gameState.board().removePieceIn(to);
		gameState.board().addPiece(from, movedPiece);
		gameState.board().addPiece(moveEvent.to(), moveEvent.pieceCaptured());
		movedPiece.unmoved();
	}

	public static List<Tile> calculateMoveTiles(GameState gameState, Color color, Tile start, Move move) {
		List<Tile> tiles = new ArrayList<>();
		Tile t = start;
		int movementFactor = color == Color.BLACK ? -1 : 1;

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

	public static List<Tile> piecesTilesByColor(GameState gameState, Color color) {
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

	public static Map<Integer, List<Tile>> legalPieceMoves(GameState gameState, Tile location) {
		if(gameState.board().pieceAt(location) == null) return new HashMap<>();

		Map<Integer, List<Tile>> movesetMap = validPieceMoves(gameState, location);

		Map<Integer, List<Tile>> legalMoveset = new HashMap<>();

		Piece piece = gameState.board().pieceAt(location);

		for(Map.Entry<Integer, List<Tile>> moveEntry : movesetMap.entrySet()) {
			Move move = piece.moveset().get(moveEntry.getKey());
			List<Tile> moveTiles = new ArrayList<>(moveEntry.getValue());

			moveTiles.removeIf(tile -> {
				Piece pieceAtTile = gameState.board().pieceAt(tile);

				boolean unchecksKing = !GameConditions.moveUnchecksKing(gameState, move, location, tile);

				return (pieceAtTile != null && pieceAtTile.type() == PieceType.KING) || unchecksKing;
			});

			legalMoveset.put(moveEntry.getKey(), moveTiles);
		}

		return legalMoveset;
	}

	public static Map<Integer, List<Tile>> pieceMovesetTiles(GameState gameState, Tile location) {
		if(gameState.board().pieceAt(location) == null) return new HashMap<>();

		Piece piece = gameState.board().pieceAt(location);

		List<Move> pieceMoveset = piece.moveset();

		Map<Integer, List<Tile>> movesetTiles = new HashMap<>();

		for(int moveIndex = 0; moveIndex < pieceMoveset.size(); moveIndex++) {
			Move move = pieceMoveset.get(moveIndex);

			List<Tile> filteredMoveTiles = calculateMoveTiles(
					gameState,
					piece.color(),
					location,
					move
			);

			movesetTiles.put(moveIndex, filteredMoveTiles);
		}

		return movesetTiles;
	}

	public static Map<Integer, List<Tile>> validPieceMoves(GameState gameState, Tile location) {
		return pieceMovesetTiles(gameState, location).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> {
					int moveIndex = e.getKey();

					List<Tile> pendingTiles = e.getValue();

					return MovesValidator.filterMoveTiles(
							gameState,
							gameState.board().pieceAt(location).moveset().get(moveIndex),
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
