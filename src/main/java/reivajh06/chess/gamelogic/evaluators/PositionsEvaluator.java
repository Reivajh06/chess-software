package reivajh06.chess.gamelogic.evaluators;

import reivajh06.services.FenParser;
import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.gamelogic.GameStateBuilder;
import reivajh06.chess.gamelogic.Position;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;
import reivajh06.chess.model.movement.Move;

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
