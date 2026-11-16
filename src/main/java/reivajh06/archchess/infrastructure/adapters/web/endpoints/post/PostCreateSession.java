package reivajh06.archchess.infrastructure.adapters.web.endpoints.post;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.datalake.events.CreateSessionEvent;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.dtos.CreateSessionRequest;
import reivajh06.archchess.infrastructure.adapters.web.dtos.CreateSessionResponse;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.EndpointOperator;

public class PostCreateSession implements EndpointOperator<CreateSessionRequest, CreateSessionResponse> {

	@Override
	public CreateSessionResponse run(EndpointContext ctx, CreateSessionRequest request) {
		GameSession session = new GameSession(request.gamemode(), request.difficulty());
		String id = ctx.sessionId();
		Player player = ctx.sessionPlayer();

		Archchess.instance().publish(new CreateSessionEvent(player.username(), session.id(), session.gamemode(), session.difficulty()));

		if(session.gamemode() == Gamemode.PVALFI) {
			Player rival = Player.plugin(request.pluginNames().getLast(), id);
			session.assignColor(player, Color.WHITE);
			session.assignColor(rival, Color.BLACK);
			Archchess.instance().registerSession(session);

			return new CreateSessionResponse(
					session.id(),
					session.gamemode(),
					session.difficulty(),
					id,
					Color.WHITE,
					session.gameState());

		} else if(session.gamemode() != Gamemode.PRACTICE && session.gamemode() != Gamemode.PVP) {
			if(session.gamemode() == Gamemode.MVM) player = Player.plugin(request.pluginNames().getFirst(), id);

			Player rival = Player.plugin(request.pluginNames().getLast(), id);
			session.assignColor(rival);
		}

		Color creatorColor = session.assignColor(player);

		Archchess.instance().registerSession(session);

		return new CreateSessionResponse(
				session.id(),
				session.gamemode(),
				session.difficulty(),
				id,
				creatorColor,
				session.gameState()
		);
	}

	@Override
	public CreateSessionRequest deserializeBody(String body) {
		return Json.fromJsonPretty(body, CreateSessionRequest.class);
	}
}
