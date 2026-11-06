package reivajh06.architecture.domain.logic.updaters;

import reivajh06.architecture.domain.model.*;
import reivajh06.architecture.domain.model.game.CastlingRights;
import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.GameStateBuilder;
import reivajh06.architecture.domain.model.movement.Move;

public class CastlingRightsUpdater implements Updater {

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        CastlingRights updatedRights = new CastlingRights();

        updateRights(updatedRights, previousState, move, movedPiece, capturedPiece, from, to);

        gameStateBuilder.castlingRights(updatedRights);
    }

    private void updateRights(CastlingRights castlingRights, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        if(canCastleKingside(Color.BLACK, previousState, movedPiece)) castlingRights.grantRight(Color.BLACK, Castling.KINGSIDE);
        if(canCastleQueenside(Color.BLACK, previousState, movedPiece)) castlingRights.grantRight(Color.BLACK, Castling.QUEENSIDE);

        if(canCastleKingside(Color.WHITE, previousState, movedPiece)) castlingRights.grantRight(Color.WHITE, Castling.KINGSIDE);
        if(canCastleQueenside(Color.WHITE, previousState, movedPiece)) castlingRights.grantRight(Color.WHITE, Castling.QUEENSIDE);
    }

    private boolean canCastleQueenside(Color color, GameState previousState, Piece movedPiece) {
        if(movedPiece.color() == color && movedPiece.type() == PieceType.KING) return false;
        if(!previousState.castlingRights().hasRight(color, Castling.QUEENSIDE)) return false;

        Tile rookQueensideTile = new Tile(File.A, color == Color.BLACK ? Rank.R8 : Rank.R1);
        Piece pieceOnQueenside = previousState.board().pieceAt(rookQueensideTile);

        if(pieceOnQueenside == null) return false;
        if(pieceOnQueenside.type() != PieceType.ROOK) return false;

		return pieceOnQueenside.color() == color;
	}

    private boolean canCastleKingside(Color color, GameState previousState, Piece movedPiece) {
        if(movedPiece.color() == color && movedPiece.type() == PieceType.KING) return false;
        if(!previousState.castlingRights().hasRight(color, Castling.KINGSIDE)) return false;

        Tile rookQueensideTile = new Tile(File.H, color == Color.BLACK ? Rank.R8 : Rank.R1);
        Piece pieceOnQueenside = previousState.board().pieceAt(rookQueensideTile);

        if(pieceOnQueenside == null) return false;
        if(pieceOnQueenside.type() != PieceType.ROOK) return false;

        return pieceOnQueenside.color() == color;
    }
}
