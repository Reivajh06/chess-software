package software.ulpgc.gamelogic;

import software.ulpgc.model.*;
import software.ulpgc.model.movement.Move;
import software.ulpgc.model.movement.MoveCondition;
import software.ulpgc.model.movement.MoveContext;
import software.ulpgc.model.movement.MoveData;

import java.util.List;

public class MovesValidator {

	private final Board board;
	private final GameController controller;

	public MovesValidator(Board board, GameController controller) {
		this.controller = controller;
		this.board = board;
	}

	public List<Tile> filterMoveTiles(Move move, Tile from, List<Tile> pendingTiles, MoveData lastMove) {
		return pendingTiles.stream().filter(pendingTile -> {
			Piece p = board.pieceAt(pendingTile);
			List<MoveCondition> conditions = move.conditions();

			if(p != null && p.color() == board.pieceAt(from).color()) return false;

			for(MoveCondition condition : conditions) {
				if(condition.satisfies(new MoveContext(lastMove, controller, board, from, pendingTile))) return true;
			}

			return false;
		}).toList();
	}
}