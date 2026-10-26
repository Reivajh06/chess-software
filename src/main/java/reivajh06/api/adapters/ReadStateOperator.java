package reivajh06.api.adapters;

import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.ports.EndpointOperator;
import reivajh06.chess.gamelogic.GameSession;

import java.util.UUID;

public class ReadStateOperator implements EndpointOperator<UUID, GameSession> {

	@Override
	public GameSession operate(UUID sessionId) {
		GameSession session = Sessions.fetchSession(sessionId);

		if(session == null) throw new ApiException(404, "Session not found");

		return session;
	}
}
