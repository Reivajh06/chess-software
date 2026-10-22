package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.CastlingRights;
import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.*;
import reivajh06.model.movement.Move;

public class CastlingRightsUpdater implements Updater {

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        CastlingRights updatedRights = new CastlingRights();

        updateWhiteRights(updatedRights, previousState, move, movedPiece, capturedPiece, from, to);
        updateBlackRights(updatedRights, previousState, move, movedPiece, capturedPiece, from, to);

        gameStateBuilder.castlingRights(updatedRights);
    }

    private void updateWhiteRights(CastlingRights castlingRights, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        if(movedPiece.color() == Color.WHITE) {
            if(movedPiece.type() == PieceType.KING) return;


        }
    }

    private void updateBlackRights(CastlingRights castlingRights, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {

    }
}
