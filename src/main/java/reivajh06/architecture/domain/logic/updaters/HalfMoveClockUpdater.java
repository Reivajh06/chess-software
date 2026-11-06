package reivajh06.architecture.domain.logic.updaters;

import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.GameStateBuilder;
import reivajh06.architecture.domain.model.Piece;
import reivajh06.architecture.domain.model.PieceType;
import reivajh06.architecture.domain.model.Tile;
import reivajh06.architecture.domain.model.movement.Move;

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
