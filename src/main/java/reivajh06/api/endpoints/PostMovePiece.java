package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameUpdater;
import reivajh06.model.Tile;

import java.util.Map;
import java.util.UUID;

public class PostMovePiece implements Handler {

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		Map<String, Object> moveRequest = Json.fromJson(ctx.body(), Map.class);

		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

		GameUpdater updater = new GameUpdater(session);

		Object moveId = moveRequest.get("moveId");

		updater.movePieceAndUpdate(
				moveId instanceof Double ? ((Double) moveId).intValue() : (Integer) moveId,
				new Tile((String) moveRequest.get("from")),
				new Tile((String) moveRequest.get("to"))
				);
	}
}
