package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameSession {

    private final GameController controller;
    private final MovesValidator validator;

    private final Gamemode gamemode;
    private final Board board;
    private GameStatus status;
    private Difficulty difficulty;
    private final List<MoveData> movesHistory;
    private final Map<PieceColor, List<Piece>> capturedPieces;
    private boolean debug = false;

    private boolean onCheck = false;
    private PieceColor turn = PieceColor.WHITE;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        this.difficulty = difficulty;
        this.gamemode = gamemode;

        status = GameStatus.ACTIVE;
        movesHistory = new ArrayList<>();
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

        if(piece == null) return new HashMap<>();
        if(onCheck && piece.type() != PieceType.KING) return new HashMap<>();

        if(turn != piece.color()) return new HashMap<>();

        List<Move> pieceMoveset = piece.moveset();

        Map<Move, List<Tile>> movesetTiles = new HashMap<>();

        for(Move move : pieceMoveset) {
            movesetTiles.put(move, validator.filterMoveTiles(move, location, controller.calculateMoveTiles(piece.color(), location, move)));
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

        board.pieceAt(to).moved();

        registerMove(from, to, pieceEaten);

        if(debug) printBoard();

        updateGameStatus(to);

        turn = turn == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public List<MoveData> movesHistory() {
        return movesHistory;
    }

    public PieceColor colorTurn() {
        return turn;
    }

    public void registerMove(Tile from, Tile to, Piece capturedPiece) {
        movesHistory.add(new MoveData(board.pieceAt(from), from, to, capturedPiece));

        if(capturedPiece != null) capturedPieces.get(capturedPiece.color()).add(capturedPiece);
    }

    public void printBoard() {
        board.printBoard();
        System.out.println();
    }

    private void updateGameStatus(Tile pieceLocation) {
        Tile kingTile = getKingThreatenedTile(pieceLocation);

        if(kingTile == null) {
            status = GameStatus.ACTIVE;
            onCheck = false;
            return;
        }

        onCheck = true;

        if(isCheckMate(kingTile)) status = board.pieceAt(kingTile).color() == PieceColor.BLACK ? GameStatus.WHITE_WIN : GameStatus.BLACK_WIN;
    }

    private Tile getKingThreatenedTile(Tile pieceLocation) {
        List<Tile> nextMovesetTiles = pieceMovesetTiles(pieceLocation).values().stream().flatMap(List::stream).collect(Collectors.toUnmodifiableList());

        return nextMovesetTiles.stream()
                .filter(t -> {
                    Piece p = board.pieceAt(t);

                    return p != null && p.type() == PieceType.KING;
                })
                .findFirst()
                .orElse(null);
    }

    private boolean isCheckMate(Tile kingTile) {
        return pieceMovesetTiles(kingTile).isEmpty();
    }
}
