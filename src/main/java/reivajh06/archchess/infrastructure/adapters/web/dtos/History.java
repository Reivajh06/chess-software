package reivajh06.archchess.infrastructure.adapters.web.dtos;

import java.util.List;

public record History(
		String player,
		List<Session> sessions
) {
}
