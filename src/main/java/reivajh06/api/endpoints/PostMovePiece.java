package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.services.FenParser;
import reivajh06.services.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.MovePieceDto;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;
import reivajh06.database.Player;
import reivajh06.datalake.events.MovePieceEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostMovePiece implements EndpointOperator<MovePieceDto, Void> {

	@Override
	public Void run(EndpointContext ctx, MovePieceDto moveRequest) {
		GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		Piece piece = session.gameState().board().pieceAt(moveRequest.from());
		GameDirector updater = new GameDirector(session);

		Player player = new Player(ctx.sessionAttribute("username"), ctx.ip());

		if(player.username() == null) player = Player.guest(ctx.ip());

		if(!session.hasTurn(player)) {
			ctx.status(HttpStatus.FORBIDDEN);
			return null;
		}

		List<Tile> allowed = updater.pieceMoves(moveRequest.from()).get(moveRequest.moveId());

		if(allowed == null || !allowed.contains(moveRequest.to())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Illegal move");
		}

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
				FenParser.gameStateToFen(session.gameState()),
				session.gameState().status()
				));

		return null;
	}

	@Override
	public MovePieceDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, MovePieceDto.class);
	}
}
