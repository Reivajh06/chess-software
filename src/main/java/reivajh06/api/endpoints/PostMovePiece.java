package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.MovePieceDto;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.gamelogic.GameSession;

import java.util.Map;
import java.util.UUID;

public class PostMovePiece extends EndpointHandler<MovePieceDto, Void> {

	@Override
	protected Void run(Context ctx, Map<String, String> pathParamMap, MovePieceDto moveRequest) {
		GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		GameDirector updater = new GameDirector(session);

		updater.movePieceAndUpdate(
				moveRequest.moveId(),
				moveRequest.from(),
				moveRequest.to()
		);

		return null;
	}

	@Override
	protected MovePieceDto deserializeBody(Context ctx) {
		return Json.fromJsonPretty(ctx.body(), MovePieceDto.class);
	}
}
