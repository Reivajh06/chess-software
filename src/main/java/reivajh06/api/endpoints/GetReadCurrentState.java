package reivajh06.api.endpoints;

import io.javalin.http.Context;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.chess.gamelogic.GameSession;

import java.util.UUID;

public class GetReadCurrentState extends EndpointHandler<GameSession> {

	@Override
	protected GameSession run(Context ctx) {
		UUID uuid = UUID.fromString(ctx.pathParam("uuid"));

		GameSession session = Sessions.fetchSession(uuid);

		if(session == null) throw new ApiException(404, "Session not found");

		return session;
	}
}
