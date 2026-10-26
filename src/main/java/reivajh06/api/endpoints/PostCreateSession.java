package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import reivajh06.FenParser;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.chess.gamelogic.Difficulty;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.gamelogic.Gamemode;
import reivajh06.chess.model.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostCreateSession implements Handler {

	@Override
	public void handle(@NotNull Context ctx) {
		Map<String, String> sessionType = Json.fromJson(ctx.body(), Map.class);

		UUID creatorId = UUID.randomUUID();
		Gamemode gamemode = Gamemode.valueOf(sessionType.get("gamemode"));
		Difficulty difficulty = Difficulty.valueOf(sessionType.get("difficulty"));

		GameSession session = new GameSession(gamemode, difficulty);
		Color creatorColor = gamemode == Gamemode.PRACTICE ?
				Color.WHITE
				: session.assignColor(creatorId);

		if(gamemode != Gamemode.PVP) {
			UUID rivalId = UUID.randomUUID();
			session.assignColor(rivalId);
		}

		Sessions.registerSession(session);

		ctx.status(HttpStatus.OK).result(jsonResponse(creatorId, creatorColor, session));
	}

	private String jsonResponse(UUID playerId, Color playerColor, GameSession session) {
		Map<String, Object> resultBody = new HashMap<>(Map.of(
				"sessionId", session.id(),
				"gamemode", session.gamemode(),
				"difficulty", session.difficulty(),
				"playerId", playerId.toString(),
				"playerColor", playerColor.name(),
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
