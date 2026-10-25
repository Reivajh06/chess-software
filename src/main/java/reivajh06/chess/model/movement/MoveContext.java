package reivajh06.chess.model.movement;

import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.model.Tile;

public record MoveContext(GameState gameState, Tile from, Tile to, MoveEvent lastMove) {
}
