package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.FenParser;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.ReadStateDto;
import reivajh06.chess.gamelogic.GameSession;

import java.util.UUID;

public class GetReadCurrentState extends EndpointHandler<ReadStateDto> {

	@Override
	protected ReadStateDto run(Context ctx) {
		UUID uuid = UUID.fromString(ctx.pathParam("sessionId"));

		GameSession session = Sessions.fetchSession(uuid);

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		return new ReadStateDto(
				session.gameState().status(),
				false,
				session.gameState().turnColor(),
				session.gameState().checkedColor(),
				FenParser.gameStateToFen(session.gameState()),
				session.gameState().eatenPieces(),
				session.gameState().movesHistory()
		);
	}
}
