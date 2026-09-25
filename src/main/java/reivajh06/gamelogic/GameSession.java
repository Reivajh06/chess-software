package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;

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
    private boolean debug = false;

    private boolean onCheck = false;
    private PieceColor turn = PieceColor.WHITE;

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

		if(onCheck && piece.type() != PieceType.KING) return new HashMap<>();

        if(isAbsolutelyPinned(location)) return new HashMap<>();

        List<Move> pieceMoveset = piece.moveset();

        Map<Move, List<Tile>> movesetTiles = new HashMap<>();

        for(Move move : pieceMoveset) {
            List<Tile> filterMoveTiles = validator.filterMoveTiles(move, location, controller.calculateMoveTiles(piece.color(), location, move));

            movesetTiles.put(move, filterMoveTiles);
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

        registerMove(from, to, pieceEaten);

        board.pieceAt(to).moved();

        if(debug) printBoard();

        updateGameStatus(to);

        turn = turn == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
    }

    private boolean isAbsolutelyPinned(Tile location) {
        Piece piece = controller.removePieceAt(location);

        if(piece == null) return false;
        if(piece.type() == PieceType.KING) return false;

        Tile kingTile = controller.kingTile(piece.color());

        boolean isPinned = controller.getPiecesTilesByColor(piece.color() == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK).stream()
                .filter(t -> {
                    Piece p = board.pieceAt(t);

                    if(p == null) return false;

                    PieceType type = p.type();

                    return type == PieceType.BISHOP || type == PieceType.QUEEN || type == PieceType.ROOK;
                })
                .filter(t -> {
                    Map<Move, List<Tile>> movesetTiles = pieceMovesetTiles(t);

                    for(List<Tile> moveTiles : movesetTiles.values()) {
                        if(moveTiles.contains(kingTile)) return true;
                    }

                    return false;
                })
                .findFirst()
                .orElse(null) != null;


        controller.addPiece(piece, location);

        return isPinned;
    }

    private boolean isKingExposed(PieceColor color) {
        Tile kingTile = controller.kingTile(color);

		return controller.getPiecesTilesByColor(color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK).stream()
				.filter(t -> {
                    Tile kingCheckedTile = kingCheckedTile(t);

                    return kingCheckedTile != null && kingCheckedTile.equals(kingTile);
                })
				.findFirst()
				.orElse(null) != null;
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

    public void registerMove(Tile from, Tile to, Piece capturedPiece) {
        movesHistory.push(new MoveData(board.pieceAt(from), from, to, capturedPiece));

        if(capturedPiece != null) capturedPieces.get(capturedPiece.color()).add(capturedPiece);
    }

    public GameStatus status() {
        return status;
    }

    public void printBoard() {
        board.printBoard();
        System.out.println();
    }

    private void updateGameStatus(Tile pieceLocation) {
        Tile kingTile = kingCheckedTile(pieceLocation);

        if(kingTile == null) {
            status = GameStatus.ACTIVE;
            onCheck = false;
            return;
        }

        onCheck = true;

        if(isCheckMate(kingTile)) status = board.pieceAt(kingTile).color() == PieceColor.BLACK ? GameStatus.WHITE_WIN : GameStatus.BLACK_WIN;
    }

    private Tile kingCheckedTile(Tile pieceLocation) {
        Piece piece = board.pieceAt(pieceLocation);
        Tile rivalKingTile = piece.color() == PieceColor.WHITE ? controller.blackKingTile() : controller.whiteKingTile();

        return pieceMovesetTiles(pieceLocation).values().stream()
                .flatMap(List::stream)
                .filter(t -> t.equals(rivalKingTile))
                .findFirst()
                .orElse(null);
    }

    private boolean isCheckMate(Tile kingTile) {
        return pieceMovesetTiles(kingTile).isEmpty();
    }
}
