package reivajh06.gamelogic.evaluators;

import reivajh06.FenParser;
import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.Piece;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveEvent;

import java.util.Deque;

public class MovesHistoryEvaluator implements Evaluator {

	@Override
	public void evaluate(GameStateBuilder gameStateBuilder, GameState transitiveState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		Deque<MoveEvent> movesHistory = transitiveState.movesHistory();

		movesHistory.addFirst(new MoveEvent(movedPiece, move, from, to, capturedPiece, FenParser.gameStateToFen(transitiveState)));

		gameStateBuilder.movesHistory(movesHistory);
	}
}
