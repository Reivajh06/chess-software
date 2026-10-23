package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.CastlingRights;
import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.*;
import reivajh06.model.movement.Move;

public class CastlingRightsUpdater implements Updater {

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        /* CASTLING UPDATE CHECKS
         * - ON ROOK MOVED, REMOVE THE CASTLING OF ITS SIDE, THIS DOES NOT AFFECT ROOK PROMOTIONS
         * - ON CASTLING RIGHT PREVIOUSLY REMOVED, JUST RETURNED SAID RIGHT REVOKED TOO
         * - CAN CHECK IF ROOK IS AVAILABLE ON SAID SIDE, AS IT IS CHECKED IF THAT RIGHT WAS PREVIOUSLY REMOVED
         * - CHECK IF MOVED PIECE IS KING, TO REVOKE ALL ITS CASTLING RIGHTS
         * - DONT MANAGE CASTLING AVAILABILITY. IF THE CASTLING CANNOT BE EXECUTED BECAUSE IS A DANGEROUS MOVE, THIS WILL
         * BE HANDLED BY THE CASTLING SPECIFIC MOVE RULE. DONT REVOKE RIGHTS WHEN THIS HAPPENS, IT DOES NOT MATTER
         */

        CastlingRights updatedRights = new CastlingRights();

        updateRights(updatedRights, previousState, move, movedPiece, capturedPiece, from, to);

        gameStateBuilder.castlingRights(updatedRights);
    }

    private void updateRights(CastlingRights castlingRights, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        if(movedPiece.color() == Color.BLACK && movedPiece.type() == PieceType.KING) return;

        if(canCastleKingside(Color.BLACK, previousState)) castlingRights.grantRight(Color.BLACK, Castling.KINGSIDE);
        if(canCastleQueenside(Color.BLACK, previousState)) castlingRights.grantRight(Color.BLACK, Castling.QUEENSIDE);

        if(canCastleKingside(Color.WHITE, previousState)) castlingRights.grantRight(Color.WHITE, Castling.KINGSIDE);
        if(canCastleQueenside(Color.WHITE, previousState)) castlingRights.grantRight(Color.WHITE, Castling.QUEENSIDE);
    }

    private boolean canCastleQueenside(Color color, GameState previousState) {
        if(!previousState.castlingRights().hasRight(color, Castling.QUEENSIDE)) return false;

        Tile rookQueensideTile = new Tile(File.H, color == Color.BLACK ? Rank.R8 : Rank.R1);
        Piece pieceOnQueenside = previousState.board().pieceAt(rookQueensideTile);

        if(pieceOnQueenside == null) return false;
        if(pieceOnQueenside.type() != PieceType.ROOK) return false;

		return pieceOnQueenside.color() == color;
	}

    private boolean canCastleKingside(Color color, GameState previousState) {
        if(!previousState.castlingRights().hasRight(color, Castling.KINGSIDE)) return false;

        Tile rookQueensideTile = new Tile(File.A, color == Color.BLACK ? Rank.R8 : Rank.R1);
        Piece pieceOnQueenside = previousState.board().pieceAt(rookQueensideTile);

        if(pieceOnQueenside == null) return false;
        if(pieceOnQueenside.type() != PieceType.ROOK) return false;

        return pieceOnQueenside.color() == color;
    }
}
