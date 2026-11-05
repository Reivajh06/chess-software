package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.services.FenParser;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.ReadStateResponseDto;
import reivajh06.chess.gamelogic.GameSession;

import java.util.Map;
import java.util.UUID;

public class GetReadCurrentState extends EndpointHandler<Void, ReadStateResponseDto> {

	@Override
	protected ReadStateResponseDto run(Context ctx, Map<String, String> pathParamMap, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		return new ReadStateResponseDto(
				session.gameState().status(),
				false,
				session.gameState().turnColor(),
				session.gameState().checkedColor(),
				FenParser.gameStateToFen(session.gameState()),
				session.gameState().eatenPieces(),
				session.gameState().movesHistory()
		);
	}

	@Override
	protected Void deserializeBody(String body) {
		return null;
	}
}
