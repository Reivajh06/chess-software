package reivajh06.archchess.architecture.domain.logic.evaluators;

import reivajh06.archchess.architecture.domain.model.Piece;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.game.GameState;
import reivajh06.archchess.architecture.domain.model.game.GameStateBuilder;
import reivajh06.archchess.architecture.domain.model.movement.Move;
import reivajh06.archchess.architecture.domain.model.movement.MoveEvent;
import reivajh06.archchess.architecture.domain.services.FenParser;

import java.util.Deque;

public class MovesHistoryEvaluator implements Evaluator {

	@Override
	public void evaluate(GameStateBuilder gameStateBuilder, GameState transitiveState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		Deque<MoveEvent> movesHistory = transitiveState.movesHistory();

		movesHistory.addFirst(new MoveEvent(movedPiece, move, from, to, capturedPiece, FenParser.gameStateToFen(transitiveState)));

		gameStateBuilder.movesHistory(movesHistory);
	}
}
