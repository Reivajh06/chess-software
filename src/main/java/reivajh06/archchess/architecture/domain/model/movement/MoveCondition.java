package reivajh06.archchess.architecture.domain.model.movement;

public interface MoveCondition {

	MoveCondition NO_CONDITION = m -> true;

	boolean satisfies(MoveContext ctx);
}
