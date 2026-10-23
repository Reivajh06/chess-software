package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.Piece;
import reivajh06.model.PieceType;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

public class HalfMoveClockUpdater implements Updater{

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        if(movedPiece.type() == PieceType.PAWN) {
            gameStateBuilder.halfMoveClock(0);
            return;
        }

        if(capturedPiece != null) {
            gameStateBuilder.halfMoveClock(0);
            return;
        }

        gameStateBuilder.halfMoveClock(previousState.halfMoveClock() + 1);
    }
}
