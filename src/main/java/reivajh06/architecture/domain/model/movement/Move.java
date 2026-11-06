package reivajh06.architecture.domain.model.movement;

public record Move(int x, int y, int repetitions, MoveCondition condition, MoveEffect effect) {
}
