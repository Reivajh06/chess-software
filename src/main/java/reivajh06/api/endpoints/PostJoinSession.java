package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Archchess;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.JoinDto;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.Color;
import reivajh06.database.Player;
import reivajh06.datalake.events.JoinSessionEvent;

import java.util.Map;
import java.util.UUID;

public class PostJoinSession extends EndpointHandler<Void, JoinDto> {

	@Override
	protected JoinDto run(Context ctx, Map<String, String> pathParamMap, Void input) {
		GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

		if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

		String id = ctx.req().getSession().getId();
		String username = ctx.sessionAttribute("username");
		Player player = new Player(username, ctx.ip());

		if(username == null) player = Player.guest(ctx.ip());

		Color assignedColor = session.assignColor(player);

		if(assignedColor == null) throw new ApiException(HttpStatus.CONFLICT, "Session is already full");

		Archchess.instance().publish(new JoinSessionEvent(
				player.username(),
				session.id().toString(),
				session.playerIdentifier(Color.BLACK).equals(player) ?
						session.playerIdentifier(Color.WHITE).username()
						: session.playerIdentifier(Color.BLACK).username()
				));

		return new JoinDto(
				id,
				assignedColor,
				session.gamemode(),
				session.difficulty(),
				session.gameState()
		);
	}

	@Override
	protected Void deserializeBody(String body) {
		return null;
	}
}
