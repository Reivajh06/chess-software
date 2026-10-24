package reivajh06.gamelogic.evaluators;

import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.Piece;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

public interface Evaluator {

	void evaluate(GameStateBuilder gameStateBuilder, GameState transitiveState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to);
}
