package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.game.Difficulty;
import reivajh06.archchess.architecture.domain.model.game.GameState;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;

public record Join(
		String playerId,
		Color playerColor,
		Gamemode gamemode,
		Difficulty difficulty,
		GameState state
) {
}
