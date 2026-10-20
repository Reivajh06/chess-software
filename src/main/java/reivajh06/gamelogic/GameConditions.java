package reivajh06.gamelogic;

import reivajh06.FenParser;
import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GameConditions {

    public static boolean moveUnchecksKing(GameState gameState, Move move, Tile from, Tile to) {
        Piece pieceMoved = gameState.board().pieceAt(from);
        Piece pieceCaptured = null;

        try {
            pieceCaptured = GameController.movePiece(gameState, move, from, to);

            if(isKingChecked(gameState, pieceMoved.color())) return false;

        } finally {
            GameController.undoMove(gameState, new MoveEvent(pieceMoved, move, from, to, pieceCaptured, FenParser.gameStateToFen(gameState)));
        }

        return true;
    }

    public static boolean isKingCheckedBy(GameState gameState, Tile tile, Map<Integer, List<Tile>> validPieceMoves) {
        Color kingCheckedColor = Color.rivalColor(gameState.board().pieceAt(tile).color());

        return validPieceMoves.values().stream().anyMatch(l -> l.contains(gameState.board().kingTile(kingCheckedColor)));
    }

    public static boolean isKingChecked(GameState gameState, Color kingColor) {
        return GameController.piecesTilesByColor(gameState, Color.rivalColor(kingColor)).stream()
                .anyMatch(t -> isKingCheckedBy(gameState, t, GameController.validPieceMoves(gameState, t)));
    }

    public static boolean isCheckMate(GameState gameState, Color kingColor) {
        if (!isKingChecked(gameState, kingColor))
            return false;

        return !canAnyPieceSaveKing(gameState, kingColor);
    }

    public static boolean canAnyPieceSaveKing(GameState gameState, Color kingColor) {
        return GameController.piecesTilesByColor(gameState, kingColor).stream()
                .anyMatch(tile ->
                        GameController.legalPieceMoves(gameState, tile).values().stream()
                                .anyMatch(moveTiles -> !moveTiles.isEmpty())
                );
    }

    public static boolean onDraw(GameState gameState) {
        return onStalemate(gameState) || onDeadPosition(gameState) || onThreefoldRepetition(gameState);
    }

    public static boolean noLegalMoves(GameState gameState, Color color) {
        return GameController.piecesTilesByColor(gameState, color).stream()
                .map(t -> GameController.legalPieceMoves(gameState, t).values())
                .flatMap(Collection::stream)
                .allMatch(Collection::isEmpty);
    }

    public static boolean onStalemate(GameState gameState) {
        return !isKingChecked(gameState, gameState.turnColor()) && noLegalMoves(gameState, gameState.turnColor());
    }

    public static boolean onDeadPosition(GameState gameState) {
        Board board = gameState.board();

        List<Tile> blackTilesRemaining = board.piecesTilesByColor(Color.BLACK);
        List<Tile> whiteTilesRemaining = board.piecesTilesByColor(Color.WHITE);

        return (piecesRemaining(board, whiteTilesRemaining, PieceType.KING) && piecesRemaining(board, blackTilesRemaining, PieceType.KING)) ||
                (piecesRemaining(board, whiteTilesRemaining, PieceType.KING, PieceType.BISHOP) && piecesRemaining(board, blackTilesRemaining, PieceType.KING)) ||
                (piecesRemaining(board, whiteTilesRemaining, PieceType.KING) && piecesRemaining(board, blackTilesRemaining, PieceType.KING, PieceType.BISHOP)) ||
                (piecesRemaining(board, whiteTilesRemaining, PieceType.KING, PieceType.KNIGHT) && piecesRemaining(board, blackTilesRemaining, PieceType.KING)) ||
                remainsDoubleBishopAndKing(board, blackTilesRemaining, whiteTilesRemaining);
    }

    private static boolean remainsDoubleBishopAndKing(Board board, List<Tile> blackTilesRemaining, List<Tile> whiteTilesRemaining) {
        if(!piecesRemaining(board, blackTilesRemaining, PieceType.KING, PieceType.BISHOP)) return false;
        if(!piecesRemaining(board, whiteTilesRemaining, PieceType.KING, PieceType.BISHOP)) return false;

        Tile blackBishopTile = blackTilesRemaining.stream().filter(t -> board.pieceAt(t).type() == PieceType.BISHOP).findFirst().orElse(null);
        Tile whiteBishopTile = whiteTilesRemaining.stream().filter(t -> board.pieceAt(t).type() == PieceType.BISHOP).findFirst().orElse(null);

        return Color.tileColor(blackBishopTile) == Color.tileColor(whiteBishopTile);
    }

    private static boolean piecesRemaining(Board board, List<Tile> tilesRemaining, PieceType... pieceTypes) {
        if(tilesRemaining.size() != pieceTypes.length) return false;

        return new HashSet<>(tilesRemaining.stream().map(t -> board.pieceAt(t).type()).toList()).containsAll(List.of(pieceTypes));
    }

    public static boolean onThreefoldRepetition(GameState gameState) {
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
