package reivajh06.architecture.domain.logic.evaluators;

import reivajh06.architecture.domain.services.FenParser;
import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.GameStateBuilder;
import reivajh06.architecture.domain.model.game.Position;
import reivajh06.architecture.domain.model.Piece;
import reivajh06.architecture.domain.model.Tile;
import reivajh06.architecture.domain.model.movement.Move;

import java.util.HashMap;
import java.util.Map;

public class PositionsEvaluator implements Evaluator {

	@Override
	public void evaluate(GameStateBuilder gameStateBuilder, GameState transitiveState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		Map<Position, Integer> positions = new HashMap<>(transitiveState.positions());

		Position position = new Position(FenParser.gameStateToFen(transitiveState));

		positions.merge(position, 1, Integer::sum);
		transitiveState.positions().merge(position, 1, Integer::sum);
		gameStateBuilder.positions(transitiveState.positions());
	}
}
