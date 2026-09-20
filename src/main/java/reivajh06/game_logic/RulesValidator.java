package reivajh06.game_logic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RulesValidator {

	public static final MoveRule EN_PASSANT = (board, from, to) -> {
		if(!isDiagonalMove(from, to)) return false;

		Piece p = board.pieceAt(from);
		Tile adjacentTile = new Tile(to.file(), from.rank());
		Piece adjacentPiece = board.pieceAt(adjacentTile);

		return adjacentPiece != null && adjacentPiece.color() != p.color();
	};

	public static final MoveRule DIAGONAL_CAPTURE = (board, from, to) -> {
		Piece pieceToCapture = board.pieceAt(to);

		return isDiagonalMove(from, to) && pieceToCapture != null;
	};

	public static final MoveRule START_TWO_FORWARD = (board, from, to) -> {
		Piece p = board.pieceAt(from);

		return p.timesMoved() == 0 && isHorizontalMove(from, to);
	};


	private final Board board;
	private final Map<Move, Set<MoveRule>> movementRules;
	private PieceColor turn;

	public RulesValidator(Board board) {
		this.board = board;
		movementRules = new HashMap<>();
		turn = PieceColor.WHITE;
	}

	public void addMoveRules(Move move, Set<MoveRule> rules) {
		movementRules.put(move, rules);
	}

	public Set<MoveRule> getMoveRules(Move move) {
		return movementRules.get(move);
	}

	public void updateMoveRules(Move move, Set<MoveRule> additionalRules) {
		Set<MoveRule> rules = movementRules.get(move);

		if(rules == null) return;

		rules.addAll(additionalRules);
	}

	public List<Tile> filterMoveTiles(Move move, Tile from, List<Tile> pendingTiles) {
		Set<MoveRule> moveRules = movementRules.get(move);

		return pendingTiles.stream().filter(pendingTile -> {
			Piece p = board.pieceAt(pendingTile);

			if(p != null && p.color() == board.pieceAt(from).color()) return false;

			if(moveRules == null) return true;

			for(MoveRule rule : moveRules) {
				if(rule.complies(board, from, pendingTile)) return true;
			}

			return false;
		}).toList();
	}

	public GameStatus getGameStatus() {
		return GameStatus.ACTIVE;
	}

	public PieceColor turn() {
		return turn;
	}

	public void nextTurn() {
		turn = turn == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
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
