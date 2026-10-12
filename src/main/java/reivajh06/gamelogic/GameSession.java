package reivajh06.gamelogic;

import reivajh06.FenParser;

import java.util.UUID;

public class GameSession {

    private final UUID id;

    private final UUID whiteId;
    private final UUID blackId;

    private final Gamemode gamemode;
    private final Difficulty difficulty;
    private GameState gameState;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        id = UUID.randomUUID();

        this.gamemode = gamemode;
        this.difficulty = difficulty;
        this.gameState = GameState.startingState();

        whiteId = UUID.randomUUID();
        blackId = gamemode == Gamemode.PRACTICE ? whiteId : UUID.randomUUID();
    }
    public UUID id() {
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

    public UUID whiteId() {
        return whiteId;
    }

    public UUID blackId() {
        return blackId;
    }

    public void gameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String toFEN() {
        return FenParser.gameStateToFen(gameState);
    }
}