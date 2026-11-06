package reivajh06.architecture.domain.logic.evaluators;

import reivajh06.architecture.domain.model.game.GameConditions;
import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.GameStateBuilder;
import reivajh06.architecture.domain.model.Color;
import reivajh06.architecture.domain.model.Piece;
import reivajh06.architecture.domain.model.Tile;
import reivajh06.architecture.domain.model.movement.Move;

public class CheckedColorEvaluator implements Evaluator {

	@Override
	public void evaluate(GameStateBuilder gameStateBuilder, GameState transitiveState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		Color kingColor = Color.rivalColor(movedPiece.color());

		gameStateBuilder.checkedColor(GameConditions.isKingChecked(transitiveState, kingColor) ? kingColor : null);
	}
}
