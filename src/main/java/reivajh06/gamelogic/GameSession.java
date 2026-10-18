package reivajh06.gamelogic;

import reivajh06.FenParser;
import reivajh06.model.PieceColor;

import java.util.Random;
import java.util.UUID;

public class GameSession {

    private final UUID id;

    private UUID whiteId;
    private UUID blackId;

    private final Gamemode gamemode;
    private final Difficulty difficulty;
    private GameState gameState;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        id = UUID.randomUUID();

        this.gamemode = gamemode;
        this.difficulty = difficulty;
        this.gameState = GameState.startingState();

        if(gamemode.equals(Gamemode.PRACTICE)) whiteId = blackId = UUID.randomUUID();
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

    public PieceColor assignColor(UUID playerId) {
        PieceColor assignedColor = null;

        if(whiteId == null) {
            if(blackId == null) {
                Random random = new Random();

                assignedColor = PieceColor.values()[random.nextInt(PieceColor.values().length)];

                if(assignedColor == PieceColor.WHITE) whiteId = playerId;
                else blackId = playerId;

            } else {
                whiteId = playerId;
                assignedColor = PieceColor.WHITE;
            }
        } else if(blackId == null) {
            blackId = playerId;
            assignedColor = PieceColor.BLACK;
        }

        return assignedColor;
    }

    public void gameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String toFEN() {
        return FenParser.gameStateToFen(gameState);
    }
}