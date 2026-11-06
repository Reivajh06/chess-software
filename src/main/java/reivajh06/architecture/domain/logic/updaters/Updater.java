package reivajh06.architecture.domain.logic.updaters;

import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.GameStateBuilder;
import reivajh06.architecture.domain.model.Piece;
import reivajh06.architecture.domain.model.Tile;
import reivajh06.architecture.domain.model.movement.Move;

public interface Updater {

	void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to);
}
