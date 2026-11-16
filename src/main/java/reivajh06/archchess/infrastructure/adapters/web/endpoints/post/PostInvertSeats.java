package reivajh06.archchess.infrastructure.adapters.web.endpoints.post;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;
import reivajh06.archchess.infrastructure.adapters.Sessions;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.EndpointOperator;

import java.util.UUID;

public class PostInvertSeats implements EndpointOperator<Void, Void> {

	@Override
	public Void run(EndpointContext ctx, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session was not found");

		if(session.gamemode() != Gamemode.PVALFI) throw new ApiException(HttpStatus.FORBIDDEN, "Cannot execute this operation given session gamemode");

		Player whitePlayer = session.playerIdentifier(Color.WHITE);
		Player blackPlayer = session.playerIdentifier(Color.BLACK);

		session.assignColor(whitePlayer, Color.BLACK);
		session.assignColor(blackPlayer, Color.WHITE);

		return null;
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
