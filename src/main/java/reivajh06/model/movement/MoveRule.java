package reivajh06.model.movement;

import reivajh06.model.Board;
import reivajh06.model.Tile;

public interface MoveRule {

	boolean complies(Board board, Tile from, Tile to);
}
