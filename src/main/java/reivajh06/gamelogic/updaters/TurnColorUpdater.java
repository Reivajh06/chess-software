package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.Color;
import reivajh06.model.Piece;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

public class TurnColorUpdater implements Updater {

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        gameStateBuilder.turnColor(Color.rivalColor(movedPiece.color()));
    }
}
