package reivajh06.api.dtos;

import reivajh06.chess.gamelogic.Difficulty;
import reivajh06.chess.gamelogic.Gamemode;

import java.util.List;

public record CreateSessionRequestDto(Gamemode gamemode, Difficulty difficulty, List<String> pluginNames) {
}
