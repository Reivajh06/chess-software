package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.game.GameStatus;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;

import java.util.List;

public record Session(
		String sessionId,
		Gamemode gamemode,
		String createdAt,
		String creator,
		String rival,
		GameStatus status,
		List<HistoricalMove> moves
) {
}
