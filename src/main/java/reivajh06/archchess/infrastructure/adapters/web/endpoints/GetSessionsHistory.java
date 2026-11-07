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
import reivajh06.archchess.infrastructure.adapters.web.dtos.HistoricalMoveDto;
import reivajh06.archchess.infrastructure.adapters.web.dtos.HistoryDto;
import reivajh06.archchess.infrastructure.adapters.web.dtos.SessionDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GetSessionsHistory implements EndpointOperator<Void, HistoryDto> {

	@Override
	public HistoryDto run(EndpointContext ctx, Void input) {
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

		List<SessionDto> sessionDtos = new ArrayList<>();

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

			List<HistoricalMoveDto> historicalMoveDtos = new ArrayList<>();

			for(int i = 0; i < sessionMoves.size(); i++) {
				MovePieceEvent move = sessionMoves.get(i);
				String nextMoveTimestamp = i + 1 < sessionMoves.size() ? sessionMoves.get(i + 1).timestamp() : null;

				PromotePieceEvent promotion = promoteEvents.stream()
						.filter(p -> p.sessionId().equals(event.sessionId()))
						.filter(p -> p.timestamp().compareTo(move.timestamp()) > 0)
						.filter(p -> nextMoveTimestamp == null || p.timestamp().compareTo(nextMoveTimestamp) < 0)
						.findFirst()
						.orElse(null);

				historicalMoveDtos.add(new HistoricalMoveDto(
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

			sessionDtos.add(new SessionDto(
					event.sessionId(),
					event.gamemode(),
					event.timestamp(),
					event.username(),
					rivalUsername,
					historicalMoveDtos.isEmpty() ? GameStatus.ACTIVE : historicalMoveDtos.getLast().status(),
					historicalMoveDtos
			));
		}

		sessionDtos.sort(Comparator.comparing(SessionDto::createdAt).reversed());

		return new HistoryDto(
				player.username(),
				sessionDtos
		);
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
