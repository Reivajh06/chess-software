package reivajh06.chess.model.movement;

public record Move(int x, int y, int repetitions, MoveCondition condition, MoveEffect effect) {
}
