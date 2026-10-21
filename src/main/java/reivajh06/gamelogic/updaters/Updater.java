package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.GameState;
import reivajh06.model.Piece;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

public interface Updater<V> {

	V update(GameState gameState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to);
}
