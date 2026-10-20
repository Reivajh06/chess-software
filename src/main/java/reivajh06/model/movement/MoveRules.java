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

		if(p.color() == Color.WHITE) {
			if(ctx.from().rank() != Board.WHITE_PAWN_START_RANK) return false;

		} else if(ctx.from().rank() != Board.BLACK_PAWN_START_RANK) {
			return false;
		}

		Tile destination = new Tile(
				ctx.from().file(),
				p.color() == Color.WHITE ?
						Rank.next(ctx.from().rank())
						: Rank.previous(ctx.from().rank())
				);

		if(board.pieceAt(destination) != null) return false;

		return DESTINATION_EMPTY.satisfies(ctx);
	};

	public static final MoveCondition SAFE_MOVE = ctx -> {
		Board board = ctx.gameState().board();

		Piece piece = board.pieceAt(ctx.from());
		List<Tile> rivalTiles = GameController.piecesTilesByColor(
				ctx.gameState(),
				piece.color() == Color.BLACK ? Color.WHITE : Color.BLACK
		);

		for(Tile rivalTile : rivalTiles) {
			Piece rivalPiece = board.pieceAt(rivalTile);
			List<Move> moveset = rivalPiece.moveset();

			for(Move move : moveset) {
				boolean isTileCompromised = GameController.calculateMoveTiles(ctx.gameState(), rivalPiece.color(), rivalTile, move).contains(ctx.to());

				if(isTileCompromised) return false;
			}
		}

		return true;
	};

	public static final MoveCondition EN_PASSANT = ctx -> {
		Tile enPassantDestination = ctx.gameState().enPassantDestination();

		if(enPassantDestination == null) return false;

        return ctx.to().equals(enPassantDestination);
    };

	public static final MoveCondition QUEENSIDE_CASTLING = ctx -> {
		Board board = ctx.gameState().board();

		Color color = board.pieceAt(ctx.from()).color();

		if(color == Color.BLACK && ctx.gameState().blackCastling().getLast() != null) return false;
		if(color == Color.WHITE && ctx.gameState().whiteCastling().getLast() != null) return false;

		for(File file = File.C; file != null && file.compareTo(ctx.from().file()) < 0; file = File.next(file)) {
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

		Color color = board.pieceAt(ctx.from()).color();

		if(color == Color.BLACK && ctx.gameState().blackCastling().getFirst() != null) return false;
		if(color == Color.WHITE && ctx.gameState().whiteCastling().getFirst() != null) return false;

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
