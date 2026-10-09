package reivajh06.gamelogic;

import java.util.UUID;

public class GameSession {

    private final String id;

    private final Gamemode gamemode;
    private final Difficulty difficulty;
    private GameState gameState;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        id = UUID.randomUUID().toString();

        this.gamemode = gamemode;
        this.difficulty = difficulty;
        this.gameState = GameState.startingState();
    }

    public String id() {
        return id;
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

    public void gameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String toFEN() {
        return gameState.toFEN();
    }
}