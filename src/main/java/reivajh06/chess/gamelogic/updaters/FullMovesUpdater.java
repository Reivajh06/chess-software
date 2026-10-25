package reivajh06.chess.gamelogic.updaters;

import reivajh06.chess.gamelogic.GameState;
import reivajh06.chess.gamelogic.GameStateBuilder;
import reivajh06.chess.model.Color;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;
import reivajh06.chess.model.movement.Move;

public class FullMovesUpdater implements Updater {

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        int fullMoves = previousState.fullMoves();

        gameStateBuilder.fullMoves(
                movedPiece.color() == Color.BLACK ?
                        fullMoves + 1
                        : fullMoves
        );
    }
}
