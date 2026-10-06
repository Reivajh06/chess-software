package reivajh06.model.movement;

import reivajh06.gamelogic.GameState;
import reivajh06.model.Tile;

public record MoveContext(GameState gameState, Tile from, Tile to, MoveEvent lastMove) {
}
