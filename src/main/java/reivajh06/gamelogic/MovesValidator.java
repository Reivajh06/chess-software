package reivajh06.gamelogic;

import reivajh06.model.Piece;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveCondition;
import reivajh06.model.movement.MoveContext;

import java.util.List;

public class MovesValidator {

	public static List<Tile> filterMoveTiles(GameSession session, Move move, Tile from, List<Tile> pendingTiles) {
		return pendingTiles.stream().filter(pendingTile -> {
			Piece p = session.board().pieceAt(pendingTile);
			List<MoveCondition> conditions = move.conditions();

			if(p != null && p.color() == session.board().pieceAt(from).color()) return false;

			for(MoveCondition condition : conditions) {
				if(condition.satisfies(new MoveContext(session, from, pendingTile))) return true;
			}

			return false;
		}).toList();
	}
}