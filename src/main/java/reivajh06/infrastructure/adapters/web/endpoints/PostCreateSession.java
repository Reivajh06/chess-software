package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.architecture.domain.model.Color;
import reivajh06.architecture.domain.model.Player;
import reivajh06.architecture.domain.model.game.GameSession;
import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.adapters.Json;
import reivajh06.infrastructure.adapters.datalake.events.CreateSessionEvent;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.web.dtos.CreateSessionRequestDto;
import reivajh06.infrastructure.adapters.web.dtos.CreateSessionResponseDto;

public class PostCreateSession implements EndpointOperator<CreateSessionRequestDto, CreateSessionResponseDto> {

	@Override
	public CreateSessionResponseDto run(EndpointContext ctx, CreateSessionRequestDto request) {
		GameSession session = new GameSession(request.gamemode(), request.difficulty());
		String id = ctx.sessionId();
		Player player = ctx.sessionPlayer();

		Color creatorColor = session.assignColor(player);

		Archchess.instance().registerSession(session);

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
