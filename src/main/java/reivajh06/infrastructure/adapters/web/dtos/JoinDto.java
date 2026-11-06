package reivajh06.infrastructure.adapters.web.dtos;

import reivajh06.architecture.domain.model.game.Difficulty;
import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.Gamemode;
import reivajh06.architecture.domain.model.Color;

public record JoinDto(
		String playerId,
		Color playerColor,
		Gamemode gamemode,
		Difficulty difficulty,
		GameState state
) {
}
