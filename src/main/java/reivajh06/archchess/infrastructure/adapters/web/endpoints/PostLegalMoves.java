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
import reivajh06.archchess.infrastructure.adapters.web.dtos.MoveDto;
import reivajh06.archchess.infrastructure.adapters.web.dtos.PieceMovesRequestDto;
import reivajh06.archchess.infrastructure.adapters.web.dtos.PieceMovesResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostLegalMoves implements EndpointOperator<PieceMovesRequestDto, PieceMovesResponseDto> {

	@Override
	public PieceMovesResponseDto run(EndpointContext ctx, PieceMovesRequestDto input) {
		Map<String, String> selectedPieceDetails = Json.fromJsonPretty(ctx.body(), Map.class);

		GameSession session = Archchess.instance().session(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		Player player = ctx.sessionPlayer();

		if(!session.hasTurn(player)) return new PieceMovesResponseDto(new ArrayList<>());

		GameDirector director = new GameDirector(session);

		List<MoveDto> pieceMoves = director.pieceMoves(new Tile(selectedPieceDetails.get("from"))).entrySet().stream()
				.map(e -> new MoveDto(e.getKey(),
						e.getValue().stream()
								.map(t -> Map.of("file", t.file().name(), "rank", t.rank().name()))
								.toList())
				).toList();

		return new PieceMovesResponseDto(pieceMoves);
	}

	@Override
	public PieceMovesRequestDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, PieceMovesRequestDto.class);
	}
}
