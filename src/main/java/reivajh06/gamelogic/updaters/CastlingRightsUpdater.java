package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.model.*;
import reivajh06.model.movement.Move;

import java.util.ArrayList;
import java.util.List;

public class CastlingRightsUpdater implements Updater {

    @Override
    public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
        List<Castling> whiteCastling = new ArrayList<>(previousState.whiteCastling());
        List<Castling> blackCastling = new ArrayList<>(previousState.blackCastling());

        if(movedPiece.color() == Color.WHITE) {
            if(movedPiece.type() == PieceType.KING) {
                whiteCastling.set(0, null);
                whiteCastling.set(1, null);
            }

            else whiteCastling = updateCastlingRights(movedPiece);
        }

        else {
            if(movedPiece.type() == PieceType.KING) {
                blackCastling.set(0, null);
                blackCastling.set(1, null);
            }

            else blackCastling = updateCastlingRights(movedPiece);
        }
    }

    private List<Castling> updateCastlingRights(Piece movedPiece) {
        List<Castling> castlingRights = new ArrayList<>(
                movedPiece.color() == Color.BLACK ?
                        session.gameState().blackCastling()
                        : session.gameState().whiteCastling()
        );

        if(castlingRights.getFirst() != null) {
            if(!canCastleKingSide(movedPiece)) castlingRights.set(0, null);

        } else if(castlingRights.getLast() != null) {
            if(!canCastleQueenSide(movedPiece)) castlingRights.set(1, null);
        }

        return castlingRights;
    }

    private boolean canCastleKingSide(Piece movedPiece) {
        Color color = movedPiece.color();
        GameState currentState = session.gameState();
        Board updatedBoard = currentState.board();

        if(movedPiece.type() == PieceType.KING) return false;

        if(color == Color.BLACK) {
            if(currentState.blackCastling().getFirst() == null) return false;

            Tile kingsideRookTile = new Tile(File.H, Board.BLACK_KING_START_RANK);
            Piece piece = updatedBoard.pieceAt(kingsideRookTile);

            if(piece == null) return false;
            if(piece.type() == PieceType.ROOK) return piece.color() == movedPiece.color() && piece.timesMoved() == 0;

        } else {
            if(currentState.whiteCastling().getFirst() == null) return false;

            Tile kingsideRookTile = new Tile(File.H, Board.WHITE_KING_START_RANK);
            Piece piece = updatedBoard.pieceAt(kingsideRookTile);

            if(piece == null) return false;
            if(piece.type() == PieceType.ROOK) return piece.color() == movedPiece.color() && piece.timesMoved() == 0;
        }

        return false;
    }

    private boolean canCastleQueenSide(Piece movedPiece) {
        Color color = movedPiece.color();
        GameState currentState = session.gameState();
        Board updatedBoard = currentState.board();

        if(movedPiece.type() == PieceType.KING) return false;

        if(color == Color.BLACK) {
            if(currentState.blackCastling().getLast() == null) return false;

            Tile queensideRookTile = new Tile(File.A, Board.BLACK_KING_START_RANK);
            Piece piece = updatedBoard.pieceAt(queensideRookTile);

            if(piece == null) return false;
            if(piece.type() == PieceType.ROOK) return piece.timesMoved() == 0;

        } else {
            if(currentState.whiteCastling().getLast() == null) return false;

            Tile queensideRookTile = new Tile(File.A, Board.WHITE_KING_START_RANK);
            Piece piece = updatedBoard.pieceAt(queensideRookTile);

            if(piece == null) return false;
            if(piece.type() == PieceType.ROOK) return piece.timesMoved() == 0;
        }

        return false;
    }
}
