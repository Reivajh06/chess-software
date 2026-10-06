package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GameConditions {

    public static boolean moveUnchecksKing(GameSession session, Move move, Tile from, Tile to) {
        Piece pieceMoved = session.board().pieceAt(from);
        Piece pieceCaptured = null;

        try {
            pieceCaptured = GameController.movePiece(session, move, from, to);

            if(isKingChecked(session, pieceMoved.color())) return false;

        } finally {
            GameController.undoMove(session, new MoveData(pieceMoved, move, from, to, pieceCaptured, session.board()));
        }

        return true;
    }

    public static boolean isKingCheckedBy(GameSession session, Tile tile, Map<Move, List<Tile>> validPieceMoves) {
        PieceColor kingCheckedColor = PieceColor.rivalColor(session.board().pieceAt(tile).color());

        return validPieceMoves.values().stream().anyMatch(l -> l.contains(session.board().kingTile(kingCheckedColor)));
    }

    public static boolean isKingChecked(GameSession session, PieceColor kingColor) {
        return GameController.piecesTilesByColor(session, PieceColor.rivalColor(kingColor)).stream()
                .anyMatch(t -> isKingCheckedBy(session, t, GameController.validPieceMoves(session, t)));
    }

    public static boolean isCheckMate(GameSession session, Tile kingTile) {
        PieceColor kingColor = session.board().pieceAt(kingTile).color();

        if (!isKingChecked(session, kingColor))
            return false;

        return !canAnyPieceSaveKing(session, kingColor);
    }

    public static boolean canAnyPieceSaveKing(GameSession session, PieceColor kingColor) {
        return GameController.piecesTilesByColor(session, kingColor).stream()
                .anyMatch(tile ->
                        GameController.legalPieceMoves(session, tile).values().stream()
                                .anyMatch(moveTiles -> !moveTiles.isEmpty())
                );
    }

    public static boolean onDraw(GameSession session) {
        return onStalemate(session) || onDeadPosition(session) || onThreefoldRepetition(session);
    }

    public static boolean noLegalMoves(GameSession session, PieceColor color) {
        return GameController.piecesTilesByColor(session, color).stream()
                .map(t -> GameController.legalPieceMoves(session, t).values())
                .flatMap(Collection::stream)
                .allMatch(Collection::isEmpty);
    }

    public static boolean onStalemate(GameSession session) {
        return !isKingChecked(session, session.gameState().turnColor()) && noLegalMoves(session, session.gameState().turnColor());
    }

    public static boolean onDeadPosition(GameSession session) {
        Board board = session.board();

        return (board.piecesRemaining(PieceColor.WHITE, PieceType.KING) && board.piecesRemaining(PieceColor.BLACK, PieceType.KING)) ||
                (board.piecesRemaining(PieceColor.WHITE, PieceType.KING, PieceType.BISHOP) && board.piecesRemaining(PieceColor.BLACK, PieceType.KING)) ||
                (board.piecesRemaining(PieceColor.WHITE, PieceType.KING) && board.piecesRemaining(PieceColor.BLACK, PieceType.KING, PieceType.BISHOP)) ||
                (board.piecesRemaining(PieceColor.WHITE, PieceType.KING, PieceType.KNIGHT) && board.piecesRemaining(PieceColor.BLACK, PieceType.KING)) ||
                (board.piecesRemaining(PieceColor.BLACK, PieceType.KING, PieceType.KNIGHT) && board.piecesRemaining(PieceColor.WHITE, PieceType.KING)) ||
                (board.piecesRemaining(PieceColor.WHITE, PieceType.KING, PieceType.BISHOP) && board.piecesRemaining(PieceColor.BLACK, PieceType.KING, PieceType.BISHOP));
    }

    public static boolean onThreefoldRepetition(GameSession session) {
        /*
        * Threefold Repetition
        A player can ask for a draw when a position is reached (or is about to be reached) at least three times in the same game.
        *  This repetition is only possible when all the pieces of the same size and color are occupying identical squares as they were before
        * and all the possible moves are also the same. As a result, if capturing en passant was previously an option that is no longer available, this position is not considered a repetition.

        When a threefold repetition occurs, the game does not necessarily need to end immediately. The game only ends in a draw if the player who has the move requests a tie.

        Most commonly, threefold repetitions happen when a player is losing and finds a perpetual check to save his game.
        *
        * */

        return false;
    }
}
