package reivajh06.chess.gamelogic.evaluators;

import reivajh06.chess.gamelogic.GameConditions;
import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.gamelogic.GameStateBuilder;
import reivajh06.chess.gamelogic.GameStatus;
import reivajh06.chess.model.Color;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;
import reivajh06.chess.model.movement.Move;

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
