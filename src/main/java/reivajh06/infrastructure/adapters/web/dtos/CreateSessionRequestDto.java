package reivajh06.infrastructure.adapters.web.dtos;

import reivajh06.architecture.domain.model.game.Difficulty;
import reivajh06.architecture.domain.model.game.Gamemode;

import java.util.List;

public record CreateSessionRequestDto(Gamemode gamemode, Difficulty difficulty, List<String> pluginNames) {
}
