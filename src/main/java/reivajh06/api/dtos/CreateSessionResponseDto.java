package reivajh06.api.dtos;

import reivajh06.chess.gamelogic.Difficulty;
import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.gamelogic.Gamemode;
import reivajh06.chess.model.Color;

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
