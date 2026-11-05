package reivajh06.api.endpoints;

import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.api.dtos.ReadStateResponseDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.services.FenParser;

import java.util.UUID;

public class GetReadCurrentState implements EndpointOperator<Void, ReadStateResponseDto> {

	@Override
	public ReadStateResponseDto run(EndpointContext ctx, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParams().get("sessionId")));

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
	public Void deserializeBody(String body) {
		return null;
	}
}
