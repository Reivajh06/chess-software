package reivajh06.archchess.architecture.domain.model.game;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.services.FenParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GameSession {

    private final UUID id;

    private final Map<Color, Player> colorPlayers;
    private final Gamemode gamemode;
    private final Difficulty difficulty;
    private GameState gameState;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        id = UUID.randomUUID();

        this.gamemode = gamemode;
        this.difficulty = difficulty;
        this.gameState = GameState.startingState();
        colorPlayers = new HashMap<>();
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

    public Color assignColor(Player colorPlayer) {
        Color assignedColor;

        Random random = new Random();

        assignedColor = Color.values()[random.nextInt(Color.values().length)];

        if(colorPlayers.get(assignedColor) == null) {
            colorPlayers.put(assignedColor, colorPlayer);
            return assignedColor;
        }

        if(colorPlayers.get(Color.rivalColor(assignedColor)) == null) {
            assignedColor = Color.rivalColor(assignedColor);
            colorPlayers.put(assignedColor, colorPlayer);
            return assignedColor;
        }

        return null;
    }

    public Player playerIdentifier(Color color) {
        return colorPlayers.get(color);
    }

    public void gameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String toFEN() {
        return FenParser.gameStateToFen(gameState);
    }

	public boolean hasTurn(Player player) {
		Color turnColor = gameState.turnColor();

        return playerIdentifier(turnColor) != null && playerIdentifier(turnColor).equals(player);
	}
}