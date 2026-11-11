package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.logic.GameDirector;
import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.Move;
import reivajh06.archchess.infrastructure.adapters.web.dtos.PieceMovesRequest;
import reivajh06.archchess.infrastructure.adapters.web.dtos.PieceMovesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostLegalMoves implements EndpointOperator<PieceMovesRequest, PieceMovesResponse> {

	@Override
	public PieceMovesResponse run(EndpointContext ctx, PieceMovesRequest input) {
		Map<String, String> selectedPieceDetails = Json.fromJsonPretty(ctx.body(), Map.class);

		GameSession session = Archchess.instance().session(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		Player player = ctx.sessionPlayer();

		if(!session.hasTurn(player)) return new PieceMovesResponse(new ArrayList<>());

		GameDirector director = new GameDirector(session);

		List<Move> pieceMoves = director.pieceMoves(new Tile(selectedPieceDetails.get("from"))).entrySet().stream()
				.map(e -> new Move(e.getKey(),
						e.getValue().stream()
								.map(t -> Map.of("file", t.file().name(), "rank", t.rank().name()))
								.toList())
				).toList();

		return new PieceMovesResponse(pieceMoves);
	}

	@Override
	public PieceMovesRequest deserializeBody(String body) {
		return Json.fromJsonPretty(body, PieceMovesRequest.class);
	}
}
