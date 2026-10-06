package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveData;

import java.util.*;
import java.util.stream.Collectors;

public class GameController {

	public static Piece movePiece(GameSession session, Move move, Tile from, Tile to) {
		return move.effect().apply(session.board(), from, to);
	}

	public static void undoMove(GameSession session, MoveData moveData) {
		if(moveData.move().effect() != null) {
			moveData.move().effect().undo(moveData);
			return;
		}

		Tile from = moveData.from();
		Tile to = moveData.to();

		Piece movedPiece = session.board().removePieceIn(to);
		session.board().addPiece(from, movedPiece);
		session.board().addPiece(moveData.to(), moveData.pieceCaptured());
		movedPiece.unmoved();
	}

	public static List<Tile> calculateMoveTiles(GameSession session, PieceColor color, Tile start, Move move) {
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

			if(session.board().pieceAt(t) != null) break;
		}

		return tiles;
	}

	public static List<Tile> piecesTilesByColor(GameSession session, PieceColor color) {
		return session.board().asStream()
				.filter(t -> session.board().pieceAt(t) != null && session.board().pieceAt(t).color() == color)
				.toList();
	}

	public static List<Tile> tilesByRank(GameSession session, Rank rank) {
		return session.board().asStream()
				.filter(t -> t.rank().equals(rank))
				.sorted(Comparator.comparing(Tile::file))
				.toList();
	}

	public static Map<Move, List<Tile>> legalPieceMoves(GameSession session, Tile location) {
		if(session.board().pieceAt(location) == null) return new HashMap<>();

		Map<Move, List<Tile>> movesetMap = validPieceMoves(session, location);

		Map<Move, List<Tile>> legalMoveset = new HashMap<>();

		for(Map.Entry<Move, List<Tile>> moveEntry : movesetMap.entrySet()) {
			Move move = moveEntry.getKey();
			List<Tile> moveTiles = new ArrayList<>(moveEntry.getValue());

			moveTiles.removeIf(tile -> !GameConditions.moveUnchecksKing(session, move, location, tile));

			legalMoveset.put(move, moveTiles);
		}

		return legalMoveset;
	}

	public static Map<Move, List<Tile>> pieceMovesetTiles(GameSession session, Tile location) {
		if(session.board().pieceAt(location) == null) return new HashMap<>();

		Piece piece = session.board().pieceAt(location);

		List<Move> pieceMoveset = piece.moveset();

		Map<Move, List<Tile>> movesetTiles = new HashMap<>();

		for(Move move : pieceMoveset) {
			List<Tile> filteredMoveTiles = calculateMoveTiles(
					session,
					piece.color(),
					location,
					move
			);

			movesetTiles.put(move, filteredMoveTiles);
		}

		return movesetTiles;
	}

	public static Map<Move, List<Tile>> validPieceMoves(GameSession session, Tile location) {
		if(session.board().pieceAt(location) == null) return new HashMap<>();
		if(session.status() != GameStatus.ACTIVE) return new HashMap<>();

		return pieceMovesetTiles(session, location).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> {
					Move move = e.getKey();
					List<Tile> pendingTiles = e.getValue();

					return MovesValidator.filterMoveTiles(
							session,
							move,
							location,
							pendingTiles
					);
				}));
	}

	private static boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}
}
