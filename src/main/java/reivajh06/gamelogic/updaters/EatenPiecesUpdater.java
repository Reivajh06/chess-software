package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.Color;
import reivajh06.model.Piece;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EatenPiecesUpdater implements Updater {

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        Map<Color, List<Piece>> eatenPieces = new HashMap<>(previousState.eatenPieces());

        if(capturedPiece != null) eatenPieces.get(capturedPiece.color()).add(capturedPiece.copy());

        gameStateBuilder.eatenPieces(eatenPieces);
    }
}
