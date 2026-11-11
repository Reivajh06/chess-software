package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.architecture.domain.services.FenParser;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.ReadStateResponse;

import java.util.UUID;

public class GetReadCurrentState implements EndpointOperator<Void, ReadStateResponse> {

	@Override
	public ReadStateResponse run(EndpointContext ctx, Void input) {
		GameSession session = Archchess.instance().session(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		return new ReadStateResponse(
				session.gameState().status(),
				false,
				session.gameState().turnColor(),
				session.gameState().checkedColor(),
				session.toFEN(),
				session.gameState().eatenPieces(),
				session.gameState().movesHistory()
		);
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
