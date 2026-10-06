package reivajh06.model.movement;

import reivajh06.gamelogic.GameSession;
import reivajh06.model.Tile;

public record MoveContext(GameSession session, Tile from, Tile to) {
}
