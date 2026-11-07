package reivajh06.archchess.architecture.domain.model.movement;

import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.game.GameState;

public record MoveContext(GameState gameState, Tile from, Tile to, MoveEvent lastMove) {
}
