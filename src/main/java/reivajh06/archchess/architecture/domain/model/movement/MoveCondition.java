package reivajh06.archchess.architecture.domain.model.movement;

public interface MoveCondition {

	MoveCondition NO_CONDITION = _ -> true;

	boolean satisfies(MoveContext ctx);
}
