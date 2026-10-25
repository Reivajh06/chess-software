package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.model.Tile;

import java.util.*;

public class PostLegalMoves implements Handler {

	@Override
	public void handle(@NotNull Context ctx) {
		Map<String, String> selectedPieceDetails = Json.fromJson(ctx.body(), Map.class);

		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

		if(session == null) {
			ctx.status(404);
			return;
		}

		GameDirector updater = new GameDirector(session);

		Map<Integer, List<Tile>> pieceMoves = updater.pieceMoves(new Tile(selectedPieceDetails.get("from")));

		ctx.status(200).result(jsonResponse(pieceMoves));
	}

	private String jsonResponse(Map<Integer, List<Tile>> pieceMoves) {
		List<Map<String, Object>> moves = new ArrayList<>();

		for(var entry : pieceMoves.entrySet()) {
			if(entry.getValue().isEmpty()) continue;

			moves.add(Map.of(
					"moveId", entry.getKey(),
					"tiles", entry.getValue()
					));
		}

		return Json.toJson(Map.of("moves", moves));
	}

}
