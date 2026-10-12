package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.FenParser;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameState;
import reivajh06.model.movement.MoveEvent;

import java.util.*;

public class GetReadCurrentState implements Handler {

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

		ctx.result(jsonResponse(session.gameState()));
	}

	private String jsonResponse(GameState gameState) {
		Map<String, Object> response = new HashMap<>();
		response.put("status", gameState.status());
		response.put("turnColor", gameState.turnColor());
		response.put("checkedColor", gameState.checkedColor());
		response.put("fen", FenParser.gameStateToFen(gameState));
		response.put("eatenPieces", gameState.eatenPieces());
		response.put("movesHistory", movesHistoryAsJson(gameState.movesHistory()));

		return Json.toJson(response);
	}

	private String movesHistoryAsJson(Deque<MoveEvent> moveEvents) {
		List<Map<String, Object>> movesHistory = new ArrayList<>();

		for(MoveEvent moveEvent : moveEvents) {
			Map<String, Object> moveEventFormatted = new HashMap<>();
			moveEventFormatted.put("pieceMoved", moveEvent.pieceMoved());
			moveEventFormatted.put("pieceCaptured", moveEvent.pieceCaptured());
			moveEventFormatted.put("from", moveEvent.from());
			moveEventFormatted.put("to", moveEvent.to());
			moveEventFormatted.put("gameStateFen", moveEvent.gameStateFen());

			movesHistory.add(moveEventFormatted);
		}

		return Json.toJson(movesHistory);
	}
}
