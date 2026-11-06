package reivajh06.architecture.domain.model.movement;

import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.Tile;

public record MoveContext(GameState gameState, Tile from, Tile to, MoveEvent lastMove) {
}
