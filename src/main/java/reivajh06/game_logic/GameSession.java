package reivajh06.game_logic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MovesetRegistry;

import java.util.*;

public class GameSession {

    private final GameController controller;
    private final RulesValidator validator;
    private final MovesetRegistry movesetRegistry;

    private final Gamemode gamemode;
    private final Board board;
    private GameStatus status;
    private Difficulty difficulty;
    private final List<MoveData> movesHistory;
    private final Map<PieceColor, List<Piece>> piecesEaten;
    private boolean debug = false;

    public GameSession(Gamemode gamemode, Difficulty difficulty) {
        this.difficulty = difficulty;
        this.gamemode = gamemode;

        status = GameStatus.ACTIVE;
        movesHistory = new ArrayList<>();
        piecesEaten = new HashMap<>(
                Map.of(
                        PieceColor.WHITE, new ArrayList<>(),
                        PieceColor.BLACK, new ArrayList<>()
                )
        );

        board = new Board();
        controller = new GameController(board);
        validator = new RulesValidator(board);
        movesetRegistry = new MovesetRegistry();

        controller.setupBoard();
    }

    public List<Tile> pieceMovesetTiles(Tile location) {
        Piece piece = board.pieceAt(location);

        if(validator.turn() != piece.color()) return new ArrayList<>();

        Set<Move> pieceMoveset = movesetRegistry.getMoveset(piece.typeName());

        List<Tile> movesetTiles = new ArrayList<>();

        for(Move move : pieceMoveset) {
            movesetTiles.addAll(validator.filterMoveTiles(move, location, controller.calculateMoveTiles(piece.color(), location, move)));
        }

        return movesetTiles;
    }

    public void move(Tile from, Tile to) {
        Piece pieceEaten = controller.movePiece(from, to);

        //register and add to pieces eaten
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

    public void registerMove(Tile from, Tile to, Piece eaten) {
        movesHistory.add(new MoveData(board.pieceAt(from), from, to, eaten));
        piecesEaten.get(eaten.color()).add(eaten);
    }

    public void printBoard() {
        board.printBoard();
        System.out.println();
    }
}
