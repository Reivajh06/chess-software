package reivajh06.model.movement;

import reivajh06.gamelogic.GameController;
import reivajh06.model.Board;
import reivajh06.model.Tile;

import java.util.ArrayList;
import java.util.List;

public interface MoveCondition {

	List<MoveCondition> NO_CONDITIONS = new ArrayList<>(List.of((_, _, _, _) -> true));

	boolean satisfies(GameController controller, Board board, Tile from, Tile to);
}
