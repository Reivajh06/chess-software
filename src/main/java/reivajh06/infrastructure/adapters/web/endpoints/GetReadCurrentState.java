package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.architecture.domain.model.game.GameSession;
import reivajh06.architecture.domain.services.FenParser;
import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.adapters.web.ApiException;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.web.HttpStatus;
import reivajh06.infrastructure.adapters.web.dtos.ReadStateResponseDto;

import java.util.UUID;

public class GetReadCurrentState implements EndpointOperator<Void, ReadStateResponseDto> {

	@Override
	public ReadStateResponseDto run(EndpointContext ctx, Void input) {
		GameSession session = Archchess.instance().session(UUID.fromString(ctx.pathParams().get("sessionId")));

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
