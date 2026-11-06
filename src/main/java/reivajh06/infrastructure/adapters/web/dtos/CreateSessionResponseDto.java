package reivajh06.infrastructure.adapters.web.dtos;

import reivajh06.architecture.domain.model.game.Difficulty;
import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.Gamemode;
import reivajh06.architecture.domain.model.Color;

import java.util.UUID;

public record CreateSessionResponseDto(
		UUID sessionId,
		Gamemode gamemode,
		Difficulty difficulty,
		String playerId,
		Color playerColor,
		GameState state
) {
}
