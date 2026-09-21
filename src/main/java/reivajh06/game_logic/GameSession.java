package reivajh06.game_logic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //Change from moveRegistry to PieceRegistry? Give the piece the moveset it has

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
        validator = new MovesValidator(board);

        controller.setupBoard();
    }

    public List<Tile> pieceMovesetTiles(Tile location) {
        Piece piece = board.pieceAt(location);

        if(piece == null) return new ArrayList<>();

        if(validator.turn() != piece.color()) return new ArrayList<>();

        List<Move> pieceMoveset = piece.moveset();

        List<Tile> movesetTiles = new ArrayList<>();

        for(Move move : pieceMoveset) {
            movesetTiles.addAll(validator.filterMoveTiles(move, location, controller.calculateMoveTiles(piece.color(), location, move)));
        }

        return movesetTiles;
    }

    public void move(Tile from, Tile to) {
        Piece pieceEaten = controller.movePiece(from, to);
        board.pieceAt(to).moved();

        registerMove(from, to, pieceEaten);

        if(debug) printBoard();

        validator.nextTurn();
    }

    public void updateGameStatus() {
        status = validator.getGameStatus();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public List<MoveData> movesHistory() {
        return movesHistory;
    }

    public PieceColor colorTurn() {
        return validator.turn();
    }

    public void registerMove(Tile from, Tile to, Piece capturedPiece) {
        movesHistory.add(new MoveData(board.pieceAt(from), from, to, capturedPiece));

        if(capturedPiece != null) capturedPieces.get(capturedPiece.color()).add(capturedPiece);
    }

    public void printBoard() {
        board.printBoard();
        System.out.println();
    }
}
