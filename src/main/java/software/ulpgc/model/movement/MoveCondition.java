package software.ulpgc.model.movement;

import software.ulpgc.gamelogic.GameController;
import software.ulpgc.model.Board;
import software.ulpgc.model.Tile;

import java.util.ArrayList;
import java.util.List;

public interface MoveCondition {

	List<MoveCondition> NO_CONDITIONS = new ArrayList<>(List.of(_ -> true));

	boolean satisfies(MoveContext ctx);
}
