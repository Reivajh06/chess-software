package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.game.Difficulty;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;

import java.util.List;

public record CreateSessionRequest(Gamemode gamemode, Difficulty difficulty, List<String> pluginNames) {
}
