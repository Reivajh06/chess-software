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
        if(board.pieceAt(tile) == null || newType == PieceType.KING) return;

        Piece piece = board.removePieceIn(tile);
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
        movesHistory.push(new MoveData(board.pieceAt(to), move, from, to, capturedPiece));

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
        if(status != GameStatus.ACTIVE) return new HashMap<>();

        return pieceMovesetTiles(location).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> validator.filterMoveTiles(
                        e.getKey(),
                        location,
                        e.getValue(),
                        movesHistory.isEmpty() ? null : movesHistory.peek()
                )));
    }

    private void updateGameStatus() {
        MoveData lastMove = movesHistory.peek();

        PieceColor kingColor = PieceColor.rivalColor(lastMove.pieceMoved().color());

        checkedColor = isKingChecked(kingColor) ? kingColor : null;

        if(checkedColor == null) return;

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
            controller.undoMove(new MoveData(pieceMoved, move, from, to, pieceCaptured));
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
}
