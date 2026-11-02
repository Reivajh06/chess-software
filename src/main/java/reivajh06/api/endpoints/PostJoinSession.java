package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.JoinDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Color;
import reivajh06.database.Player;

import java.util.Map;
import java.util.UUID;

public class PostJoinSession extends EndpointHandler<Void, JoinDto> {

	@Override
	protected JoinDto run(Context ctx, Map<String, String> pathParamMap, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		String id = ctx.req().getSession().getId();
		Color assignedColor = session.assignColor(new Player(ctx.sessionAttribute("username"), ctx.ip()));

		if(assignedColor == null) throw new ApiException(HttpStatus.CONFLICT, "Session is already full");

		return new JoinDto(
				id,
				assignedColor,
				session.gamemode(),
				session.difficulty(),
				session.gameState()
		);
	}

	@Override
	protected Void deserializeBody(Context ctx) {
		return null;
	}
}
