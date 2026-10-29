package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.JoinDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Color;

import java.util.Map;
import java.util.UUID;

public class PostJoinSession extends EndpointHandler<Void, JoinDto> {

	@Override
	protected JoinDto run(Map<String, String> pathParamMap, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		UUID playerId = UUID.randomUUID();
		Color assignedColor = session.assignColor(playerId);

		if(assignedColor == null) throw new ApiException(HttpStatus.CONFLICT, "Session is already full");

		return new JoinDto(
				playerId,
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
