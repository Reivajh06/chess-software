package reivajh06.api.endpoints;

import io.javalin.http.Context;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.dtos.CreateSessionRequestDto;
import reivajh06.api.dtos.CreateSessionResponseDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Color;

import java.util.Map;
import java.util.UUID;

public class PostCreateSession extends EndpointHandler<CreateSessionRequestDto, CreateSessionResponseDto> {

	@Override
	protected CreateSessionResponseDto run(Context ctx, Map<String, String> pathParamMap, CreateSessionRequestDto request) {
		GameSession session = new GameSession(request.gamemode(), request.difficulty());
		UUID playerId = UUID.randomUUID();
		Color creatorColor = session.assignColor(playerId);
		session.registerPlayer(creatorColor, "PLAYER");

		Sessions.registerSession(session);

		return new CreateSessionResponseDto(
				session.id(),
				session.gamemode(),
				session.difficulty(),
				playerId,
				creatorColor,
				session.gameState()
		);
	}

	@Override
	protected CreateSessionRequestDto deserializeBody(Context ctx) {
		return Json.fromJsonPretty(ctx.body(), CreateSessionRequestDto.class);
	}
}
