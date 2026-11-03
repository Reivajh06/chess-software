package reivajh06.api.endpoints;

import io.javalin.http.Context;
import reivajh06.Archchess;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.dtos.CreateSessionRequestDto;
import reivajh06.api.dtos.CreateSessionResponseDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Color;
import reivajh06.database.Player;
import reivajh06.datalake.events.CreateSessionEvent;

import java.util.Map;
import java.util.UUID;

public class PostCreateSession extends EndpointHandler<CreateSessionRequestDto, CreateSessionResponseDto> {

	@Override
	protected CreateSessionResponseDto run(Context ctx, Map<String, String> pathParamMap, CreateSessionRequestDto request) {
		GameSession session = new GameSession(request.gamemode(), request.difficulty());
		String id = ctx.req().getSession().getId();
		Player player = new Player(ctx.sessionAttribute("username"), ctx.ip());

		if(ctx.sessionAttribute("username") == null) player = Player.guest(ctx.ip());

		Color creatorColor = session.assignColor(player);

		Sessions.registerSession(session);

		Archchess.instance().publish(new CreateSessionEvent(player.username(), session.id(), session.gamemode()));

		return new CreateSessionResponseDto(
				session.id(),
				session.gamemode(),
				session.difficulty(),
				id,
				creatorColor,
				session.gameState()
		);
	}

	@Override
	protected CreateSessionRequestDto deserializeBody(Context ctx) {
		return Json.fromJsonPretty(ctx.body(), CreateSessionRequestDto.class);
	}
}
