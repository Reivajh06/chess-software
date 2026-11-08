package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.architecture.domain.model.game.GameStatus;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.datalake.EventReader;
import reivajh06.archchess.infrastructure.adapters.datalake.events.CreateSessionEvent;
import reivajh06.archchess.infrastructure.adapters.datalake.events.JoinSessionEvent;
import reivajh06.archchess.infrastructure.adapters.datalake.events.MovePieceEvent;
import reivajh06.archchess.infrastructure.adapters.datalake.events.PromotePieceEvent;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.HistoricalMove;
import reivajh06.archchess.infrastructure.adapters.web.dtos.History;
import reivajh06.archchess.infrastructure.adapters.web.dtos.Session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GetSessionsHistory implements EndpointOperator<Void, History> {

	@Override
	public History run(EndpointContext ctx, Void input) {
		Player player = ctx.sessionPlayer();

		if(player.username().startsWith("guest#")) throw new ApiException(HttpStatus.FORBIDDEN, "Player not registered");

		EventReader eventReader = Archchess.instance().eventReader();

		List<JoinSessionEvent> joinSessionEvents = eventReader.events(JoinSessionEvent.class);

		Set<String> joinedSessionIds = joinSessionEvents.stream()
				.filter(j -> j.username().equals(player.username()))
				.map(JoinSessionEvent::sessionId)
				.collect(Collectors.toSet());

		List<CreateSessionEvent> createSessionEvents = eventReader.eventsBy(CreateSessionEvent.class,
				c -> c.username().equals(player.username()) || joinedSessionIds.contains(c.sessionId()));

		List<MovePieceEvent> moveEvents = eventReader.events(MovePieceEvent.class).stream()
				.sorted(Comparator.comparing(MovePieceEvent::timestamp))
				.toList();

		List<PromotePieceEvent> promoteEvents = eventReader.events(PromotePieceEvent.class);

		List<Session> sessions = new ArrayList<>();

		for(CreateSessionEvent event : createSessionEvents) {
			String rivalUsername = joinSessionEvents.stream()
					.filter(j -> j.sessionId().equals(event.sessionId()))
					.filter(j -> !j.username().equals(event.username()))
					.map(JoinSessionEvent::username)
					.findFirst()
					.orElse(null);

			List<MovePieceEvent> sessionMoves = moveEvents.stream()
					.filter(m -> m.sessionId().equals(event.sessionId()))
					.toList();

			List<HistoricalMove> historicalMoves = new ArrayList<>();

			for(int i = 0; i < sessionMoves.size(); i++) {
				MovePieceEvent move = sessionMoves.get(i);
				String nextMoveTimestamp = i + 1 < sessionMoves.size() ? sessionMoves.get(i + 1).timestamp() : null;

				PromotePieceEvent promotion = promoteEvents.stream()
						.filter(p -> p.sessionId().equals(event.sessionId()))
						.filter(p -> p.timestamp().compareTo(move.timestamp()) > 0)
						.filter(p -> nextMoveTimestamp == null || p.timestamp().compareTo(nextMoveTimestamp) < 0)
						.findFirst()
						.orElse(null);

				historicalMoves.add(new HistoricalMove(
						i + 1,
						move.username(),
						move.pieceType(),
						move.from(),
						move.to(),
						promotion == null ? null : promotion.promotedTo(),
						promotion == null ? move.stateFen() : promotion.stateFen(),
						promotion == null ? move.status() : promotion.status(),
						move.timestamp()
				));
			}

			sessions.add(new Session(
					event.sessionId(),
					event.gamemode(),
					event.timestamp(),
					event.username(),
					rivalUsername,
					historicalMoves.isEmpty() ? GameStatus.ACTIVE : historicalMoves.getLast().status(),
					historicalMoves
			));
		}

		sessions.sort(Comparator.comparing(Session::createdAt).reversed());

		return new History(
				player.username(),
				sessions
		);
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
