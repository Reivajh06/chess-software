package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.model.Tile;

import java.util.Map;
import java.util.UUID;

public class PostMovePiece implements Handler {

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		Map<String, Object> moveRequest = Json.fromJson(ctx.body(), Map.class);

		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

		if(session == null) {
			ctx.status(HttpStatus.NOT_FOUND);
			return;
		}

		GameDirector updater = new GameDirector(session);

		Object moveId = moveRequest.get("moveId");

		updater.movePieceAndUpdate(
				moveId instanceof Double ? ((Double) moveId).intValue() : (Integer) moveId,
				new Tile((String) moveRequest.get("from")),
				new Tile((String) moveRequest.get("to"))
				);

		ctx.status(HttpStatus.OK);
	}
}
