package reivajh06.chess.gamelogic.evaluators;

import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.gamelogic.GameStateBuilder;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;
import reivajh06.chess.model.movement.Move;

public interface Evaluator {

	void evaluate(GameStateBuilder gameStateBuilder, GameState transitiveState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to);
}
