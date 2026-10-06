package reivajh06.model.movement;

import reivajh06.gamelogic.GameController;
import reivajh06.model.Board;
import reivajh06.model.PieceColor;
import reivajh06.model.Tile;

public record MoveContext(MoveData lastMove, GameController controller, Board board, Tile from, Tile to, PieceColor colorChecked) {
}
