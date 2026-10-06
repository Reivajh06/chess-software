package reivajh06.gamelogic;

import reivajh06.model.Board;
import reivajh06.model.movement.MoveData;

import java.util.Stack;
import java.util.UUID;

public class GameSession {

    private final String id;

    private Board board;
    private final Gamemode gamemode;
    private final Difficulty difficulty;
    private GameState gameState;
    private GameStatus status;
    private Stack<MoveData> movesHistory;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        id = UUID.randomUUID().toString();

        this.gamemode = gamemode;
        this.difficulty = difficulty;
        this.gameState = GameState.startingState();

        board = new Board();
        status = GameStatus.ACTIVE;
        movesHistory = new Stack<>();
    }

    public String id() {
        return id;
    }

    public Board board() {
        return board;
    }

    public Gamemode gamemode() {
        return gamemode;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public GameState gameState() {
        return gameState;
    }

    public GameStatus status() {
        return status;
    }
}