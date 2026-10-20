package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.FenParser;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameState;
import reivajh06.model.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostJoinSession implements Handler {

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

		UUID playerId = UUID.randomUUID();
		Color assignedColor = session.assignColor(playerId);

		if(assignedColor == null) {
			ctx.status(409);
			return;
		}

		ctx.status(200).result(jsonResponse(playerId, assignedColor, session));
	}

	private String jsonResponse(UUID playerId, Color playerColor, GameSession session) {
		Map<String, Object> resultBody = new HashMap<>(Map.of(
				"playerId", playerId.toString(),
				"playerColor", playerColor.name(),
				"gamemode", session.gamemode(),
				"difficulty", session.difficulty(),
				"state", extractGameStateInfo(session.gameState())
		));

		return Json.toJson(resultBody);
	}

	private Map<String, Object> extractGameStateInfo(GameState gameState) {
		Map<String, Object> state = new HashMap<>();
		state.put("status", gameState.status());
		state.put("turnColor", gameState.turnColor());
		state.put("checkedColor", gameState.checkedColor());
		state.put("waitingForOpponent", false);
		state.put("fen", FenParser.gameStateToFen(gameState));
		state.put("moves", new ArrayList<>());
		return state;
	}
}
