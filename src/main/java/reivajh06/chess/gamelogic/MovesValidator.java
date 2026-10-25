package reivajh06.chess.gamelogic;

import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;
import reivajh06.chess.model.movement.Move;
import reivajh06.chess.model.movement.MoveCondition;
import reivajh06.chess.model.movement.MoveContext;
import reivajh06.chess.model.movement.MoveEvent;

import java.util.List;

public class MovesValidator {

	public static List<Tile> filterMoveTiles(GameState gameState, Move move, Tile from, List<Tile> pendingTiles, MoveEvent lastMove) {
		return pendingTiles.stream().filter(pendingTile -> {
			Piece p = gameState.board().pieceAt(pendingTile);
			MoveCondition condition = move.condition();

			if(p != null && p.color() == gameState.board().pieceAt(from).color()) return false;

            return condition.satisfies(new MoveContext(gameState, from, pendingTile, lastMove));

        }).toList();
	}
}