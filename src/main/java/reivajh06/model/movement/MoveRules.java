package reivajh06.model.movement;
import reivajh06.gamelogic.GameController;
import reivajh06.model.*;

import java.util.List;

public class MoveRules {

	public static final MoveCondition DESTINATION_EMPTY = ctx -> ctx.gameState().board().pieceAt(ctx.to()) == null;

	public static final MoveCondition DIAGONAL_CAPTURE = ctx -> {
		Piece pieceToCapture = ctx.gameState().board().pieceAt(ctx.to());

		return isDiagonalMove(ctx.from(), ctx.to()) && pieceToCapture != null;
	};

	public static final MoveCondition START_TWO_FORWARD = (ctx) -> {
		Board board = ctx.gameState().board();

		Piece p = board.pieceAt(ctx.from());

		if(board.pieceAt(new Tile(
				ctx.from().file(),
				p.color() == PieceColor.WHITE ? Rank.next(ctx.from().rank()) : Rank.previous(ctx.from().rank()))
		) != null) return false;

		return p.timesMoved() == 0 && isVerticalMove(ctx.from(), ctx.to()) && DESTINATION_EMPTY.satisfies(ctx);
	};

	public static final MoveCondition SAFE_MOVE = ctx -> {
		Board board = ctx.gameState().board();

		Piece piece = board.pieceAt(ctx.from());
		List<Tile> rivalTiles = GameController.piecesTilesByColor(
				ctx.gameState(),
				piece.color() == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK
		);

		for(Tile rivalTile : rivalTiles) {
			Piece rivalPiece = board.pieceAt(rivalTile);
			List<Move> moveset = rivalPiece.moveset();

			for(Move move : moveset) {
				boolean isTileCompromised = GameController.calculateMoveTiles(ctx.gameState(), rivalPiece.color(), rivalTile, move).stream()
						.filter(t -> t.equals(ctx.to()))
						.findFirst()
						.orElse(null) != null;

				if(isTileCompromised) return false;
			}
		}

		return true;
	};

	public static final MoveCondition EN_PASSANT = ctx -> {
		Board board = ctx.gameState().board();
		Piece p = board.pieceAt(ctx.from());
		Tile adjacentTile = new Tile(ctx.to().file(), ctx.from().rank());
		Piece adjacentPiece = board.pieceAt(adjacentTile);

		if(adjacentPiece == null) return false;

		if(adjacentPiece.color() == p.color()) return false;
		if(adjacentPiece.type() != PieceType.PAWN) return false;
		if(adjacentPiece.timesMoved() > 1) return false;

		MoveEvent lastMove = ctx.lastMove();

		if(lastMove == null) return false;
		if(!lastMove.to().equals(adjacentTile)) return false;

		return isVerticalMove(lastMove.from(), lastMove.to()) && Math.abs(lastMove.from().rank().number - lastMove.to().rank().number) == 2;
	};

	public static final MoveCondition QUEENSIDE_CASTLING = ctx -> {
		Board board = ctx.gameState().board();
		PieceColor checkedColor = ctx.gameState().checkedColor();

		Piece king = board.pieceAt(ctx.from());

		if(king.timesMoved() != 0) return false;
		if(checkedColor == king.color()) return false;

		Tile leftRookTile = GameController.tilesByRank(ctx.gameState(), ctx.from().rank()).getFirst();
		Piece rook = board.pieceAt(leftRookTile);

		if(rook == null) return false;
		if(rook.type() != PieceType.ROOK) return false;
		if(rook.timesMoved() != 0) return false;

		for(File file = File.B; file != null && file.compareTo(ctx.from().file()) < 0; file = File.next(file)) {
			Tile tile = new Tile(file, ctx.from().rank());

			if(board.pieceAt(tile) != null) return false;
			if(!SAFE_MOVE.satisfies(new MoveContext(
					ctx.gameState(),
					ctx.from(),
					tile,
					ctx.lastMove()))
			) return false;
		}

		return true;
	};

	public static final MoveCondition KINGSIDE_CASTLING = ctx -> {
		Board board = ctx.gameState().board();
		PieceColor checkedColor = ctx.gameState().checkedColor();

		Piece king = board.pieceAt(ctx.from());

		if(king.timesMoved() != 0) return false;
		if(checkedColor == king.color()) return false;

		Tile rightRookTile = GameController.tilesByRank(ctx.gameState(), ctx.from().rank()).getLast();

		Piece rook = board.pieceAt(rightRookTile);

		if(rook == null) return false;
		if(rook.type() != PieceType.ROOK) return false;
		if(rook.timesMoved() != 0) return false;

		for(File file = File.next(ctx.from().file()); file != null && file.compareTo(File.H) < 0; file = File.next(file)) {
			Tile tile = new Tile(file, ctx.from().rank());

			if(board.pieceAt(tile) != null) return false;
			if(!SAFE_MOVE.satisfies(new MoveContext(
					ctx.gameState(),
					ctx.from(),
					tile,
					ctx.lastMove()))
			) return false;
		}

		return true;
	};

	private static boolean isDiagonalMove(Tile from, Tile to) {
		int xOffset = Math.abs(from.rank().number - to.rank().number);
		int yOffset = Math.abs(from.file().code - to.file().code);

		return (xOffset == 1) && (yOffset == 1);
	}

	private static boolean isVerticalMove(Tile from, Tile to) {
		return to.file().code - from.file().code == 0;
	}

	private static boolean isHorizontalMove(Tile from, Tile to) {
		return to.rank().number - from.rank().number == 0;
	}
}
