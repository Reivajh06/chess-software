package reivajh06.game_logic;

import reivajh06.model.Board;
import reivajh06.model.PieceColor;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RulesValidator {

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
			for(MoveRule rule : moveRules) {
				if(!rule.complies(board, from, pendingTile)) return false;
			}

			return true;
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
}
