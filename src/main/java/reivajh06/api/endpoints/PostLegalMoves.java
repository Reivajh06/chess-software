package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Json;
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

public class PostLegalMoves extends EndpointHandler<PieceMovesRequestDto, PieceMovesResponseDto> {

	@Override
	protected PieceMovesResponseDto run(Context ctx, Map<String, String> pathParamMap, PieceMovesRequestDto input) {
		Map<String, String> selectedPieceDetails = Json.fromJsonPretty(ctx.body(), Map.class);

		GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		if(!session.hasTurn(new Player(ctx.sessionAttribute("username"), ctx.ip()))) new PieceMovesResponseDto(new ArrayList<>());

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
	protected PieceMovesRequestDto deserializeBody(Context ctx) {
		return Json.fromJsonPretty(ctx.body(), PieceMovesRequestDto.class);
	}
}
