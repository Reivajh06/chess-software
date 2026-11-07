package reivajh06.archchess.infrastructure.adapters.web.dtos;

import java.util.List;

public record HistoryDto(
		String player,
		List<SessionDto> sessions
) {
}
