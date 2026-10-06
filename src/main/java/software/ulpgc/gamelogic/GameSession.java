package software.ulpgc.gamelogic;

import software.ulpgc.model.Board;
import software.ulpgc.model.movement.MoveData;

import java.util.Stack;

public record GameSession(Board board, Stack<MoveData> movesHistory, GameState state, GameStatus status) {

    public static GameSession startingSession() {
        return new GameSession(new Board(), new Stack<>(), GameState.startingState(), GameStatus.ACTIVE);
    }
}