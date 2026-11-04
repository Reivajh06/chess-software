package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Archchess;
import reivajh06.FenParser;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.MovePieceDto;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Piece;
import reivajh06.database.Player;
import reivajh06.datalake.events.MovePieceEvent;

import java.util.Map;
import java.util.UUID;

public class PostMovePiece extends EndpointHandler<MovePieceDto, Void> {

	@Override
	protected Void run(Context ctx, Map<String, String> pathParamMap, MovePieceDto moveRequest) {
		GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		Piece piece = session.gameState().board().pieceAt(moveRequest.from());
		GameDirector updater = new GameDirector(session);

		Player player = new Player(ctx.sessionAttribute("username"), ctx.ip());

		if(player.username() == null) player = Player.guest(ctx.ip());

		if(!session.hasTurn(player)) return null;

		updater.movePieceAndUpdate(
				moveRequest.moveId(),
				moveRequest.from(),
				moveRequest.to(
		));

		Archchess.instance().publish(new MovePieceEvent(
				player.username(),
				session.id(),
				moveRequest.from(),
				moveRequest.to(),
				piece.type(),
				FenParser.gameStateToFen(session.gameState())
				));
		return null;
	}

	@Override
	protected MovePieceDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, MovePieceDto.class);
	}
}
