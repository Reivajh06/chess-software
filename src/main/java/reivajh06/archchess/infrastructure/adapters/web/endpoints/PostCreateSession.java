package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.datalake.events.CreateSessionEvent;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.dtos.CreateSessionRequestDto;
import reivajh06.archchess.infrastructure.adapters.web.dtos.CreateSessionResponseDto;

public class PostCreateSession implements EndpointOperator<CreateSessionRequestDto, CreateSessionResponseDto> {

	@Override
	public CreateSessionResponseDto run(EndpointContext ctx, CreateSessionRequestDto request) {
		GameSession session = new GameSession(request.gamemode(), request.difficulty());
		String id = ctx.sessionId();
		Player player = ctx.sessionPlayer();

		Color creatorColor = session.assignColor(player);

		Archchess.instance().registerSession(session);

		Archchess.instance().publish(new CreateSessionEvent(player.username(), session.id(), session.gamemode(), session.difficulty()));

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
