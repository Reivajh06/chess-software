package software.ulpgc.gamelogic;

import software.ulpgc.model.*;
import software.ulpgc.model.movement.Move;
import software.ulpgc.model.movement.MoveData;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameSession {

    private final GameController controller;
    private final MovesValidator validator;

    private final Gamemode gamemode;
    private final Board board;
    private GameStatus status;
    private Difficulty difficulty;
    private final Stack<MoveData> movesHistory;
    private final Map<PieceColor, List<Piece>> capturedPieces;

    private PieceColor checkedColor = null;
    private PieceColor turn = PieceColor.WHITE;

    private boolean debug = false;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        this.difficulty = difficulty;
        this.gamemode = gamemode;

        status = GameStatus.ACTIVE;
        movesHistory = new Stack<>();
        capturedPieces = new HashMap<>(
                Map.of(
                        PieceColor.WHITE, new ArrayList<>(),
                        PieceColor.BLACK, new ArrayList<>()
                )
        );

        board = new Board();
        controller = new GameController(board);
        validator = new MovesValidator(board, controller);

        controller.setupBoard();
    }

    public Map<Move, List<Tile>> legalPieceMoves(Tile location) {
        if(controller.getPieceIn(location) == null) return new HashMap<>();

        Map<Move, List<Tile>> movesetMap = validPieceMoves(location);

        Map<Move, List<Tile>> legalMoveset = new HashMap<>();

        PieceColor color = controller.getPieceIn(location).color();

        for(Map.Entry<Move, List<Tile>> moveEntry : movesetMap.entrySet()) {
            Move move = moveEntry.getKey();
            List<Tile> moveTiles = new ArrayList<>(moveEntry.getValue());

            moveTiles.removeIf(tile -> !moveUnchecksKing(move, location, tile));

            legalMoveset.put(move, moveTiles);
        }

        return legalMoveset;
    }

    public void move(Move move, Tile from, Tile to) {
        if(status != GameStatus.ACTIVE) return;
        if(board.pieceAt(from) == null) return;

        Piece pieceEaten = controller.movePiece(
                move,
                from,
                to
        );

        registerMove(move, from, to, pieceEaten);

        Piece movedPiece = board.pieceAt(to);

        if(move.effect() == null) movedPiece.moved();

        if(debug) printBoard();

        updateGameStatus();

        turn = PieceColor.rivalColor(turn);
    }

    public void promotePieceIn(Tile tile, PieceType newType) {
        if(board.pieceAt(tile) == null || newType == PieceType.KING) return;

        Piece piece = board.removePieceIn(tile);

        if(!piece.isPromotable()) return;
        if(piece.color() == PieceColor.BLACK && tile.rank() != GameController.WHITE_KING_START_RANK) return;
        if(piece.color() == PieceColor.WHITE && tile.rank() != GameController.BLACK_KING_START_RANK) return;

        Piece newPiece;

        newPiece = switch(newType) {
            case PieceType.BISHOP -> PieceFactory.createBishop(piece.color());
            case PieceType.QUEEN ->  PieceFactory.createQueen(piece.color());
            case PieceType.KNIGHT -> PieceFactory.createKnight(piece.color());
            case PieceType.ROOK ->  PieceFactory.createRook(piece.color());
            default -> null;
        };

        if(newPiece == null) {
            board.addPiece(tile, piece);
            return;
        }

        board.addPiece(tile, newPiece);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Stack<MoveData> movesHistory() {
        return movesHistory;
    }

    public PieceColor colorTurn() {
        return turn;
    }

    public GameStatus status() {
        return status;
    }

    public void registerMove(Move move, Tile from, Tile to, Piece capturedPiece) {
        movesHistory.push(new MoveData(board.pieceAt(to), move, from, to, capturedPiece, board.copy()));

        if(capturedPiece != null) capturedPieces.get(capturedPiece.color()).add(capturedPiece);
    }

    public void undoMove() {
        if(movesHistory.isEmpty()) return;

        MoveData top = movesHistory.pop();

        capturedPieces.get(top.pieceCaptured().color()).remove(top.pieceCaptured());
    }

    public void printBoard() {
        board.printBoard();
        System.out.println();
    }

    private Map<Move, List<Tile>> pieceMovesetTiles(Tile location) {
        if(controller.getPieceIn(location) == null) return new HashMap<>();

        Piece piece = board.pieceAt(location);

        List<Move> pieceMoveset = piece.moveset();

        Map<Move, List<Tile>> movesetTiles = new HashMap<>();

        for(Move move : pieceMoveset) {
            List<Tile> filteredMoveTiles = controller.calculateMoveTiles(
                    piece.color(),
                    location,
                    move
            );

            movesetTiles.put(move, filteredMoveTiles);
        }

        return movesetTiles;
    }

    private Map<Move, List<Tile>> validPieceMoves(Tile location) {
        if(controller.getPieceIn(location) == null) return new HashMap<>();
        if(status != GameStatus.ACTIVE) return new HashMap<>();

        return pieceMovesetTiles(location).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> validator.filterMoveTiles(
                        e.getKey(),
                        location,
                        e.getValue(),
                        movesHistory.isEmpty() ? null : movesHistory.peek(),
                        checkedColor
                )));
    }

    private void updateGameStatus() {
        MoveData lastMove = movesHistory.peek();

        PieceColor kingColor = PieceColor.rivalColor(lastMove.pieceMoved().color());

        checkedColor = isKingChecked(kingColor) ? kingColor : null;

        if(checkedColor == null) {
            status = onDraw() ? GameStatus.DRAW : GameStatus.ACTIVE;
            return;
        }

        Tile kingTile = controller.kingTile(kingColor);

        if(isCheckMate(kingTile)) status = board.pieceAt(kingTile).color() == PieceColor.BLACK ? GameStatus.WHITE_WIN : GameStatus.BLACK_WIN;
    }

    private boolean isKingCheckedBy(Tile tile, Map<Move, List<Tile>> validPieceMoves) {
        PieceColor kingCheckedColor = PieceColor.rivalColor(controller.getPieceIn(tile).color());

        return validPieceMoves.values().stream().anyMatch(l -> l.contains(controller.kingTile(kingCheckedColor)));
    }

    private boolean isKingChecked(PieceColor kingColor) {
        return controller.piecesTilesByColor(PieceColor.rivalColor(kingColor)).stream()
                .anyMatch(t -> isKingCheckedBy(t, validPieceMoves(t)));
    }

    private boolean moveUnchecksKing(Move move, Tile from, Tile to) {
        Piece pieceMoved = controller.getPieceIn(from);
        Piece pieceCaptured = null;

        try {
            pieceCaptured = controller.movePiece(move, from, to);

            if(isKingChecked(pieceMoved.color())) return false;

        } finally {
            controller.undoMove(new MoveData(pieceMoved, move, from, to, pieceCaptured, board));
        }

        return true;
    }

    private boolean isCheckMate(Tile kingTile) {
        PieceColor kingColor = controller.getPieceIn(kingTile).color();

        if (!isKingChecked(kingColor))
            return false;

        return !canAnyPieceSaveKing(kingColor);
    }

    private boolean canAnyPieceSaveKing(PieceColor kingColor) {
        return controller.piecesTilesByColor(kingColor).stream()
                .anyMatch(tile ->
                        legalPieceMoves(tile).values().stream()
                                .anyMatch(moveTiles -> !moveTiles.isEmpty())
                );
    }

    private boolean onDraw() {
        return onStalemate() || onDeadPosition() || onThreefoldRepetition() || onDrawAgreement();
    }

    private boolean onStalemate() {
        /*
         * A draw by stalemate happens when the player who needs to move has no legal moves and his king is not in check
         * (otherwise, that would be a checkmate!).
         *  For a stalemate to happen, the move that produced the position has to be legal.
         * */
        return false;
    }

    private boolean onDeadPosition() {
        /*
        * A dead position happens when neither player can legally checkmate the opponent's king.
        *  If the game reaches this situation and the move that generated the position is legal,
        *  the game ends in a tie.
        *
        * One of the most common instances of dead positions is when there is insufficient material for either player to checkmate their opponent. This type of draw can happen even when one of the players runs out of time, provided the other player does not have enough material to deliver checkmate.

        Here are the piece combinations that lead to a draw by insufficient material:

        King vs. king
        King and bishop vs. king
        King and knight vs. king
        King and bishop vs. king and bishop of the same color as the opponent's bishop
        * */
        return false;
    }

    private boolean onDrawAgreement() {
        /*
        * Mutual Agreement
        If for any reason, both players agree to a draw,
        *  the game immediately ends, and both win half a point. For this type of tie to happen, one of the players must propose a draw, and the other player must accept it.
        *  If the other player refuses the offer or keeps playing, the game is not tied.
        * */

        return false;
    }

    private boolean onThreefoldRepetition() {
        /*
        * Threefold Repetition
        A player can ask for a draw when a position is reached (or is about to be reached) at least three times in the same game. This repetition is only possible when all the pieces of the same size and color are occupying identical squares as they were before, and all the possible moves are also the same. As a result, if capturing en passant was previously an option that is no longer available, this position is not considered a repetition.

        When a threefold repetition occurs, the game does not necessarily need to end immediately. The game only ends in a draw if the player who has the move requests a tie.

        Most commonly, threefold repetitions happen when a player is losing and finds a perpetual check to save his game.
        *
        * */

        return false;
    }

}
