package reivajh06.game_logic;

import reivajh06.model.Board;
import reivajh06.model.Tile;

public interface MoveRule {

	boolean comply(Board board, Tile from, Tile to);
}
