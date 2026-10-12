package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.FenParser;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.gamelogic.Difficulty;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.Gamemode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostCreateSession implements Handler {

	private static final int SUCCESS = 201;
	private static final int UNAVAILABLE_MODE = 501;

	@Override
	public void handle(@NotNull Context ctx) {
		Map<String, String> sessionType = Json.fromJson(ctx.body(), Map.class);

		Gamemode gamemode = Gamemode.valueOf(sessionType.get("gamemode"));
		Difficulty difficulty = Difficulty.valueOf(sessionType.get("difficulty"));

		GameSession session = new GameSession(gamemode, difficulty);

		Sessions.registerSession(session);

		ctx.status(gamemode != Gamemode.PRACTICE ? UNAVAILABLE_MODE : SUCCESS).result(jsonResponse(session));
	}

	private String jsonResponse(GameSession session) {
		Map<String, Object> resultBody = new HashMap<>(Map.of(
				"sessionId", session.id(),
				"gamemode", session.gamemode(),
				"difficulty", session.difficulty(),
				"whiteId", session.whiteId(),
				"blackId", session.blackId(),
				"state", extractGameStateInfo(session.gameState())
		));

		return Json.toJson(resultBody);
	}

	private Map<String, Object> extractGameStateInfo(GameState gameState) {
		Map<String, Object> state = new HashMap<>();
		state.put("status", gameState.status());
		state.put("turn", gameState.turnColor());
		state.put("checkedColor", gameState.checkedColor());
		state.put("waitingForOpponent", true);
		state.put("fen", FenParser.gameStateToFen(gameState));
		state.put("moves", new ArrayList<>());
		return state;
	}
}
