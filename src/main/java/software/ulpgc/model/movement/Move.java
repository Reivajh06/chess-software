package software.ulpgc.model.movement;

import java.util.List;

public record Move(int x, int y, int repetitions, List<MoveCondition> conditions, MoveEffect effect) {
}
