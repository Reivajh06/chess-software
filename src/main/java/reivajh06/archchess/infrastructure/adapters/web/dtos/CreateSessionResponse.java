package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.game.Difficulty;
import reivajh06.archchess.architecture.domain.model.game.GameState;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;

import java.util.UUID;

public record CreateSessionResponse(
		UUID sessionId,
		Gamemode gamemode,
		Difficulty difficulty,
		String playerId,
		Color playerColor,
		GameState state
) {
}
