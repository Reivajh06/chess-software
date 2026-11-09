package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.logic.GameDirector;
import reivajh06.archchess.architecture.domain.model.PieceType;
import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.datalake.events.MovePieceEvent;
import reivajh06.archchess.infrastructure.adapters.datalake.events.RequestMoveEvent;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.RequestMovePiece;
import reivajh06.plugins.Plugin;
import reivajh06.plugins.PluginConfiguration;
import reivajh06.plugins.PluginResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetRequestMove implements EndpointOperator<Void, RequestMovePiece> {

	@Override
	public RequestMovePiece run(EndpointContext ctx, Void input) {
		GameSession session = Archchess.instance().session(UUID.fromString(ctx.pathParams().get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		Player player = session.playerIdentifier(session.gameState().turnColor());
		GameDirector director = new GameDirector(session);

		Plugin plugin = Archchess.instance().plugin(player.username().split("#")[0]);
		plugin.sendParameters(new PluginConfiguration(PluginConfiguration.Difficulty.valueOf(session.difficulty().name()), 50));

		PluginResponse response = plugin.requestMove(session.toFEN());

		if(plugin.type() == Plugin.PluginType.AI) {
			Tile selectedTile = new Tile(response.message().get("selected_tile"));
			response = plugin.requestMove(session.toFEN(), director.pieceMoves(selectedTile).values().stream()
					.flatMap(List::stream)
					.map(t -> selectedTile.name() + t.name())
					.collect(Collectors.joining(" "))
			);
		}

		if(response.message().containsKey("error")) throw new ApiException(HttpStatus.BAD_REQUEST, response.message().get("error"));

		Tile from = new Tile(response.message().get("from"));
		Tile to = new Tile(response.message().get("to"));
		PieceType promoteTo = response.message().get("promote_to") != null ?
				PieceType.valueOf(response.message().get("promote_to").toUpperCase())
				: null;

		int moveId = director.pieceMoves(from).entrySet().stream()
				.filter(e -> e.getValue().contains(to))
				.findFirst()
				.orElseThrow()
				.getKey();

		Archchess.instance().publish(new RequestMoveEvent(player.username()));
		Archchess.instance().publish(new MovePieceEvent(
				player.username(),
				session.id(),
				moveId,
				from,
				to,
				promoteTo,
				session.toFEN(),
				session.gameState().status())
		);

		director.movePieceAndUpdate(moveId, from, to);

		if(promoteTo != null) director.promotePiece(to, promoteTo);

		return new RequestMovePiece(
				plugin.name(),
				moveId,
				from,
				to,
				promoteTo,
				response.details()
		);
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
