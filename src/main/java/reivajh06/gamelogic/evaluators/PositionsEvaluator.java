package reivajh06.gamelogic.evaluators;

import reivajh06.FenParser;
import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.gamelogic.Position;
import reivajh06.model.Piece;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

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
