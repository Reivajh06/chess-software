package software.ulpgc.gamelogic;

import software.ulpgc.model.*;
import software.ulpgc.model.movement.Move;
import software.ulpgc.model.movement.MoveData;

import java.util.*;
import java.util.stream.Collectors;

public class GameSession {

    private final String id;

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
        id = UUID.randomUUID().toString();
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

        turn = PieceColor.rivalColor(turn);

        updateGameStatus();
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

    public String id() {
        return id;
    }

    public void registerMove(Move move, Tile from, Tile to, Piece capturedPiece) {
        movesHistory.push(new MoveData(board.pieceAt(to), move, from, to, capturedPiece, board.copy()));

        if(capturedPiece != null) capturedPieces.get(capturedPiece.color()).add(capturedPiece);
    }

    public void undoMove() {
        if(movesHistory.isEmpty()) return;

        MoveData lastMove = movesHistory.pop();

        controller.undoMove(new MoveData(lastMove.pieceMoved(), lastMove.move(), lastMove.from(), lastMove.to(), lastMove.pieceCaptured(), board));

        turn = lastMove.pieceMoved().color();

        if(debug) controller.printBoard();

        if(lastMove.pieceCaptured() == null) return;
        capturedPieces.get(lastMove.pieceCaptured().color()).remove(lastMove.pieceCaptured());
    }

    public Map<PieceColor, List<Piece>> capturedPieces() {
        return capturedPieces;
    }

    public Piece pieceAt(Tile tile) {
        return board.pieceAt(tile);
    }

    public void printBoard() {
        controller.printBoard();
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
        return onStalemate() || onDeadPosition() || onThreefoldRepetition();
    }

    private boolean noLegalMoves(PieceColor color) {
        return controller.piecesTilesByColor(color).stream()
                .map(t -> legalPieceMoves(t).values())
                .flatMap(Collection::stream)
                .allMatch(Collection::isEmpty);
    }

    private boolean onStalemate() {
        return !isKingChecked(colorTurn()) && noLegalMoves(colorTurn());
    }

    private boolean onDeadPosition() {
        return (piecesRemaining(PieceColor.WHITE, PieceType.KING) && piecesRemaining(PieceColor.BLACK, PieceType.KING)) ||
                (piecesRemaining(PieceColor.WHITE, PieceType.KING, PieceType.BISHOP) && piecesRemaining(PieceColor.BLACK, PieceType.KING)) ||
                (piecesRemaining(PieceColor.WHITE, PieceType.KING) && piecesRemaining(PieceColor.BLACK, PieceType.KING, PieceType.BISHOP)) ||
                (piecesRemaining(PieceColor.WHITE, PieceType.KING, PieceType.KNIGHT) && piecesRemaining(PieceColor.BLACK, PieceType.KING)) ||
                (piecesRemaining(PieceColor.BLACK, PieceType.KING, PieceType.KNIGHT) && piecesRemaining(PieceColor.WHITE, PieceType.KING)) ||
                (piecesRemaining(PieceColor.WHITE, PieceType.KING, PieceType.BISHOP) && piecesRemaining(PieceColor.BLACK, PieceType.KING, PieceType.BISHOP));
    }

    private boolean piecesRemaining(PieceColor color, PieceType... pieceTypes) {
        List<Tile> remainingPiecesTiles = controller.piecesTilesByColor(color);

        if(remainingPiecesTiles.size() != pieceTypes.length) return false;

        return remainingPiecesTiles.stream()
                .map(t -> board.pieceAt(t).type())
                .collect(Collectors.toSet())
                .containsAll(List.of(pieceTypes));
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
