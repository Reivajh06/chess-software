package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveCondition;

import java.util.List;

public class MovesValidator {

	public static final MoveCondition EN_PASSANT = (_, board, from, to) -> {
		if(!isDiagonalMove(from, to)) return false;

		Piece p = board.pieceAt(from);
		Tile adjacentTile = new Tile(to.file(), from.rank());
		Piece adjacentPiece = board.pieceAt(adjacentTile);

		return adjacentPiece != null && adjacentPiece.color() != p.color();
	};

	public static final MoveCondition DIAGONAL_CAPTURE = (_, board, from, to) -> {
		Piece pieceToCapture = board.pieceAt(to);

		return isDiagonalMove(from, to) && pieceToCapture != null;
	};

	public static final MoveCondition START_TWO_FORWARD = (_, board, from, to) -> {
		Piece p = board.pieceAt(from);

		return p.timesMoved() == 0 && isHorizontalMove(from, to);
	};

	public static final MoveCondition SAFE_MOVE = (controller, board, from, to) -> {
		Piece piece = board.pieceAt(from);
		List<Tile> rivalTiles = controller.piecesTilesByColor(piece.color() == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);

		for(Tile rivalTile : rivalTiles) {
			Piece rivalPiece = board.pieceAt(rivalTile);
			List<Move> moveset = rivalPiece.moveset();

			for(Move move : moveset) {
				boolean isTileCompromised = controller.calculateMoveTiles(rivalPiece.color(), rivalTile, move).stream()
						.filter(t -> t == to)
						.findFirst()
						.orElse(null) != null;

				if(isTileCompromised) return false;
			}
		}

		return true;
	};

	public static final MoveCondition QUEENSIDE_CASTLING = (controller, board, from, to) -> {
		Piece king = board.pieceAt(from);

		if(king.timesMoved() != 0) return false;

		Tile leftRookTile = controller.tilesByRank(from.rank()).getFirst();

		if(board.pieceAt(leftRookTile).timesMoved() != 0) return false;

		for(File file = File.B; file != null && file.compareTo(from.file()) < 0; file = File.next(file)) {
			if(board.pieceAt(new Tile(file, from.rank())) != null) return false;
			if(!SAFE_MOVE.satisfies(controller, board, from, new Tile(file, from.rank()))) return false;
		}

		return true;
	};

	public static final MoveCondition KINGSIDE_CASTLING = (controller, board, from, to) -> {
		Piece king = board.pieceAt(from);

		if(king.timesMoved() != 0) return false;

		Tile leftRookTile = controller.tilesByRank(from.rank()).getLast();

		if(board.pieceAt(leftRookTile).timesMoved() != 0) return false;

		for(File file = from.file(); file != null && file.compareTo(File.H) < 0; file = File.next(file)) {
			if(board.pieceAt(new Tile(file, from.rank())) != null) return false;
			if(!SAFE_MOVE.satisfies(controller, board, from, new Tile(file, from.rank()))) return false;
		}

		return true;
	};

	private final Board board;
	private final GameController controller;

	public MovesValidator(Board board, GameController controller) {
		this.controller = controller;
		this.board = board;
	}

	public List<Tile> filterMoveTiles(Move move, Tile from, List<Tile> pendingTiles) {
		return pendingTiles.stream().filter(pendingTile -> {
			Piece p = board.pieceAt(pendingTile);
			List<MoveCondition> conditions = move.conditions();

			if(p != null && p.color() == board.pieceAt(from).color()) return false;

			for(MoveCondition condition : conditions) {
				if(condition.satisfies(controller, board, from, pendingTile)) return true;
			}

			return false;
		}).toList();
	}

	private static boolean isDiagonalMove(Tile from, Tile to) {
		int xOffset = Math.abs(from.rank().number - to.rank().number);
		int yOffset = Math.abs(from.file().code - to.file().code);

		return (xOffset == 1) && (yOffset == 1);
	}

	private static boolean isHorizontalMove(Tile from, Tile to) {
		return to.file().code - from.file().code == 0;
	}

	private static boolean isVerticalMove(Tile from, Tile to) {
		return to.rank().number - from.rank().number == 0;
	}

	private static boolean validTile(Tile t) {
		return t.file().code <= File.H.code &&
				t.file().code >= File.A.code &&
				t.rank().number <= 8 &&
				t.rank().number > 0;
	}
}