package reivajh06.api.endpoints;

import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.services.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.MoveDto;
import reivajh06.api.dtos.PieceMovesRequestDto;
import reivajh06.api.dtos.PieceMovesResponseDto;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Tile;
import reivajh06.database.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostLegalMoves implements EndpointOperator<PieceMovesRequestDto, PieceMovesResponseDto> {

	@Override
	public PieceMovesResponseDto run(EndpointContext ctx, PieceMovesRequestDto input) {
		Map<String, String> selectedPieceDetails = Json.fromJsonPretty(ctx.body(), Map.class);

		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParams().get("sessionId")));

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
