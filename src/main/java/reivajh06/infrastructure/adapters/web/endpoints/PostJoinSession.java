package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.adapters.Sessions;
import reivajh06.infrastructure.adapters.web.ApiException;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.web.HttpStatus;
import reivajh06.infrastructure.adapters.web.dtos.JoinDto;
import reivajh06.architecture.domain.model.game.GameSession;
import reivajh06.architecture.domain.model.Color;
import reivajh06.architecture.domain.model.Player;
import reivajh06.infrastructure.adapters.datalake.events.JoinSessionEvent;

import java.util.UUID;

public class PostJoinSession implements EndpointOperator<Void, JoinDto> {

	@Override
	public JoinDto run(EndpointContext ctx, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		String id = ctx.sessionId();
		Player player = ctx.sessionPlayer();

		Color assignedColor = session.assignColor(player);

		if(assignedColor == null) throw new ApiException(HttpStatus.CONFLICT, "Session is already full");

		Archchess.instance().publish(new JoinSessionEvent(
				player.username(),
				session.id().toString(),
				session.playerIdentifier(Color.BLACK).equals(player) ?
						session.playerIdentifier(Color.WHITE).username()
						: session.playerIdentifier(Color.BLACK).username()
				));

		return new JoinDto(
				id,
				assignedColor,
				session.gamemode(),
				session.difficulty(),
				session.gameState()
		);
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
