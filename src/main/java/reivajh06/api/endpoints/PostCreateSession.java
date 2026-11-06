package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.Sessions;
import reivajh06.api.EndpointContext;
import reivajh06.api.dtos.CreateSessionRequestDto;
import reivajh06.api.dtos.CreateSessionResponseDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Color;
import reivajh06.database.Player;
import reivajh06.datalake.events.CreateSessionEvent;
import reivajh06.services.Json;

public class PostCreateSession implements EndpointOperator<CreateSessionRequestDto, CreateSessionResponseDto> {

	@Override
	public CreateSessionResponseDto run(EndpointContext ctx, CreateSessionRequestDto request) {
		GameSession session = new GameSession(request.gamemode(), request.difficulty());
		String id = ctx.sessionId();
		Player player = ctx.sessionPlayer();

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
	public CreateSessionRequestDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, CreateSessionRequestDto.class);
	}
}
