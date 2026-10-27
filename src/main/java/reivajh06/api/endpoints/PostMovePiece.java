package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.MovePieceDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.model.Tile;

import java.util.Map;
import java.util.UUID;

public class PostMovePiece extends EndpointHandler {

	@Override
	protected Object run(Context ctx) {
		Map<String, Object> moveRequest = Json.fromJson(ctx.body(), Map.class);

		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		GameDirector updater = new GameDirector(session);

		Object moveId = moveRequest.get("moveId");

		updater.movePieceAndUpdate(
				moveId instanceof Double ? ((Double) moveId).intValue() : (Integer) moveId,
				new Tile((String) moveRequest.get("from")),
				new Tile((String) moveRequest.get("to"))
		);

		return null;
	}
}
