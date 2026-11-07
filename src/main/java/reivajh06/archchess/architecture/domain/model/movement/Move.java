package reivajh06.archchess.architecture.domain.model.movement;

public record Move(int x, int y, int repetitions, MoveCondition condition, MoveEffect effect) {
}
