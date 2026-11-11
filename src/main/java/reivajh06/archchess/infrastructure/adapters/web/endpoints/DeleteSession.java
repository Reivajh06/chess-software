package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Sessions;
import reivajh06.archchess.infrastructure.adapters.datalake.events.DeleteSessionEvent;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;

import java.util.UUID;

public class DeleteSession implements EndpointOperator<Void, Void> {

	@Override
	public Void run(EndpointContext ctx, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");
		if(!session.playerIdentifier(Color.WHITE).equals(ctx.sessionPlayer()) && !session.playerIdentifier(Color.BLACK).equals(ctx.sessionPlayer())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Player does not have the rights to delete this session");
		}

		Sessions.removeSession(session.id());
		Archchess.instance().publish(new DeleteSessionEvent(
				ctx.sessionPlayer().username(),
				session.id(),
				session.gamemode(),
				session.gameState().status(),
				session.toFEN()
		));
		return null;
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
