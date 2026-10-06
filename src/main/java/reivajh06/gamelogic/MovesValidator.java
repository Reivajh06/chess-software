package reivajh06.gamelogic;

import reivajh06.model.Board;
import reivajh06.model.Piece;
import reivajh06.model.PieceColor;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveCondition;
import reivajh06.model.movement.MoveContext;
import reivajh06.model.movement.MoveData;

import java.util.List;

public class MovesValidator {

	private final Board board;
	private final GameController controller;

	public MovesValidator(Board board, GameController controller) {
		this.controller = controller;
		this.board = board;
	}

	public List<Tile> filterMoveTiles(Move move, Tile from, List<Tile> pendingTiles, MoveData lastMove, PieceColor checkedColor) {
		return pendingTiles.stream().filter(pendingTile -> {
			Piece p = board.pieceAt(pendingTile);
			List<MoveCondition> conditions = move.conditions();

			if(p != null && p.color() == board.pieceAt(from).color()) return false;

			for(MoveCondition condition : conditions) {
				if(condition.satisfies(new MoveContext(lastMove, controller, board, from, pendingTile, checkedColor))) return true;
			}

			return false;
		}).toList();
	}
}