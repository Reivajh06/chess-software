package reivajh06.api.dto;

import reivajh06.chess.gamelogic.Difficulty;
import reivajh06.chess.gamelogic.Gamemode;

import java.util.List;

public record CreateSessionDto(Gamemode gamemode, Difficulty difficulty, List<String> pluginNames) {
}
