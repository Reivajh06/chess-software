package reivajh06.chess.model.movement;

public interface MoveCondition {

	MoveCondition NO_CONDITION = _ -> true;

	boolean satisfies(MoveContext ctx);
}
