package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveCondition;

import java.util.ArrayList;
import java.util.List;

public class MovesValidator {

	public static final MoveCondition EN_PASSANT = (board, from, to) -> {
		if(!isDiagonalMove(from, to)) return false;

		Piece p = board.pieceAt(from);
		Tile adjacentTile = new Tile(to.file(), from.rank());
		Piece adjacentPiece = board.pieceAt(adjacentTile);

		return adjacentPiece != null && adjacentPiece.color() != p.color();
	};

	public static final MoveCondition DIAGONAL_CAPTURE = (board, from, to) -> {
		Piece pieceToCapture = board.pieceAt(to);

		return isDiagonalMove(from, to) && pieceToCapture != null;
	};

	public static final MoveCondition START_TWO_FORWARD = (board, from, to) -> {
		Piece p = board.pieceAt(from);

		return p.timesMoved() == 0 && isHorizontalMove(from, to);
	};

	private final Board board;
	private PieceColor turn;
	private boolean onCheck = false;

	public MovesValidator(Board board) {
		this.board = board;
		turn = PieceColor.WHITE;
	}

	public List<Tile> filterMoveTiles(Move move, Tile from, List<Tile> pendingTiles) {
		if(onCheck && board.pieceAt(from).type() != PieceType.KING) return new ArrayList<>();

		return pendingTiles.stream().filter(pendingTile -> {
			Piece p = board.pieceAt(pendingTile);
			List<MoveCondition> conditions = move.conditions();

			if(p != null && p.color() == board.pieceAt(from).color()) return false;

			for(MoveCondition condition : conditions) {
				if(condition.satisfies(board, from, pendingTile)) return true;
			}

			return false;
		}).toList();
	}

	public PieceColor turn() {
		return turn;
	}

	public void nextTurn() {
		turn = turn == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
	}

	public void onCheck(boolean onCheck) {
		this.onCheck = onCheck;
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