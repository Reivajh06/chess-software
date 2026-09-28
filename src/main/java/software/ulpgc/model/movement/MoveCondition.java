package software.ulpgc.model.movement;

import java.util.ArrayList;
import java.util.List;

public interface MoveCondition {

	List<MoveCondition> NO_CONDITIONS = new ArrayList<>(List.of(_ -> true));

	boolean satisfies(MoveContext ctx);
}
