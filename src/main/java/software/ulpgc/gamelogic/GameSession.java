package software.ulpgc.gamelogic;

import software.ulpgc.model.*;
import software.ulpgc.model.movement.Move;
import software.ulpgc.model.movement.MoveData;

import java.util.*;

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

    public Map<Move, List<Tile>> pieceMovesetTiles(Tile location) {
        if(status != GameStatus.ACTIVE) return new HashMap<>();

        Piece piece = board.pieceAt(location);

        if(isAbsolutelyPinned(location)) return new HashMap<>();

        List<Move> pieceMoveset = piece.moveset();

        Map<Move, List<Tile>> movesetTiles = new HashMap<>();

        for(Move move : pieceMoveset) {
            List<Tile> filteredMoveTiles = validator.filterMoveTiles(
                    move,
                    location,
                    controller.calculateMoveTiles(
                            piece.color(),
                            location, move),
                    movesHistory.isEmpty() ? null : movesHistory.peek()
            );

            if(checkedColor != null && checkedColor == piece.color() && !moveUnchecksKing(move, location, filteredMoveTiles)) continue;
            movesetTiles.put(move, filteredMoveTiles);
        }

        return movesetTiles;
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
        movedPiece.moved();

        if(movedPiece.type() == PieceType.PAWN) {
            if(movedPiece.color() == PieceColor.BLACK && to.rank() == GameController.WHITE_KING_START_RANK) movedPiece.enablePromotion();
            if(movedPiece.color() == PieceColor.WHITE && to.rank() == GameController.BLACK_KING_START_RANK) movedPiece.enablePromotion();
        }

        if(debug) printBoard();

        updateGameStatus();

        turn = PieceColor.rivalColor(turn);
    }

    public void promotePieceIn(Tile tile, PieceType newType) {
        if(board.pieceAt(tile) == null) return;

        board.pieceAt(tile).promote(newType);
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
        movesHistory.push(new MoveData(board.pieceAt(to), move, from, to, capturedPiece));

        if(capturedPiece != null) capturedPieces.get(capturedPiece.color()).add(capturedPiece);
    }

    private boolean isAbsolutelyPinned(Tile location) {
        Piece piece = controller.removePieceAt(location);

        try {
            if(piece == null) return false;

            if(piece.type() == PieceType.KING) return false;

            return controller.piecesTilesByColor(PieceColor.rivalColor(piece.color())).stream()
                    .filter(t -> {
                        Piece p = board.pieceAt(t);

                        if(p == null) return false;

                        PieceType type = p.type();

                        return type == PieceType.BISHOP || type == PieceType.QUEEN || type == PieceType.ROOK;
                    })
                    .anyMatch(this::isKingCheckedBy);

        } finally {
            controller.addPiece(piece, location);
        }
    }

    private void updateGameStatus() {
        MoveData lastMove = movesHistory.peek();

        PieceColor kingColor = PieceColor.rivalColor(lastMove.pieceMoved().color());

        checkedColor = isKingCheckedBy(lastMove.to()) ? kingColor : null;

        if(checkedColor == null) return;

        Tile kingTile = controller.kingTile(kingColor);

        if(isCheckMate(kingTile)) status = board.pieceAt(kingTile).color() == PieceColor.BLACK ? GameStatus.WHITE_WIN : GameStatus.BLACK_WIN;
    }

    private boolean moveUnchecksKing(Move move, Tile location, List<Tile> filteredMoveTiles) {
        Piece p = board.pieceAt(location);

        return filteredMoveTiles.stream().anyMatch(t -> {
            controller.movePiece(move, location, t);

            return !isKingChecked(p.color());
        });
    }

    private boolean isKingCheckedBy(Tile tile) {
        PieceColor color = board.pieceAt(tile).color();

        return pieceMovesetTiles(tile).values().stream().anyMatch(l -> l.contains(controller.kingTile(PieceColor.rivalColor(color))));
    }

    private boolean isKingChecked(PieceColor kingColor) {
        return controller.piecesTilesByColor(PieceColor.rivalColor(kingColor)).stream()
                .anyMatch(this::isKingCheckedBy);
    }

    private boolean isCheckMate(Tile kingTile) {
        return pieceMovesetTiles(kingTile).values().stream().allMatch(List::isEmpty);
    }

    public void printBoard() {
        board.printBoard();
        System.out.println();
    }
}
