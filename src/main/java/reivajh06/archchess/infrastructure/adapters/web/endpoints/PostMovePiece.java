package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.logic.GameDirector;
import reivajh06.archchess.architecture.domain.model.Piece;
import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.datalake.events.MovePieceEvent;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.MovePiece;

import java.util.List;
import java.util.UUID;

public class PostMovePiece implements EndpointOperator<MovePiece, Void> {

	@Override
	public Void run(EndpointContext ctx, MovePiece moveRequest) {
		if(moveRequest.from() == null || moveRequest.to() == null) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Either source or destination tile is missing");
		}

		GameSession session = Archchess.instance().session(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		Piece piece = session.gameState().board().pieceAt(moveRequest.from());
		GameDirector updater = new GameDirector(session);

		Player player = ctx.sessionPlayer();

		if(!session.hasTurn(player)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Player tried to move on invalid turn");
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
				moveRequest.moveId(),
				moveRequest.from(),
				moveRequest.to(),
				piece.type(),
				session.toFEN(),
				session.gameState().status()
				));

		return null;
	}

	@Override
	public MovePiece deserializeBody(String body) {
		return Json.fromJsonPretty(body, MovePiece.class);
	}
}
