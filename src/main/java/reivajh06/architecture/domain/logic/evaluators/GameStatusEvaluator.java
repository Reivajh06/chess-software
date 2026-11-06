package reivajh06.architecture.domain.logic.evaluators;

import reivajh06.architecture.domain.model.game.GameConditions;
import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.GameStateBuilder;
import reivajh06.architecture.domain.model.game.GameStatus;
import reivajh06.architecture.domain.model.Color;
import reivajh06.architecture.domain.model.Piece;
import reivajh06.architecture.domain.model.Tile;
import reivajh06.architecture.domain.model.movement.Move;

public class GameStatusEvaluator implements Evaluator {

	@Override
	public void evaluate(GameStateBuilder gameStateBuilder, GameState transitiveState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		Color rivalColor = Color.rivalColor(movedPiece.color());

		if (GameConditions.isCheckMate(transitiveState, rivalColor)) {
			gameStateBuilder.status(
					movedPiece.color() == Color.BLACK ?
							GameStatus.BLACK_WIN
							: GameStatus.WHITE_WIN
			);

			return;
		}

		gameStateBuilder.status(
				GameConditions.onDraw(transitiveState) ?
				GameStatus.DRAW
				: GameStatus.ACTIVE
		);
	}
}
