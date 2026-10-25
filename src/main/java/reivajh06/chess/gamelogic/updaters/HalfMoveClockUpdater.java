package reivajh06.chess.gamelogic.updaters;

import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.gamelogic.GameStateBuilder;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.PieceType;
import reivajh06.chess.model.Tile;
import reivajh06.chess.model.movement.Move;

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
