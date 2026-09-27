package software.ulpgc.model.movement;

import software.ulpgc.gamelogic.GameController;
import software.ulpgc.model.Board;
import software.ulpgc.model.Tile;

public record MoveContext(MoveData lastMove, GameController controller, Board board, Tile from, Tile to) {
}
