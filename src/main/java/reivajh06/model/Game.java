package reivajh06.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    private final GameController gameController;
    private Gamemode gamemode;
    private final Board board;
    private GameStatus status;
    private Difficulty difficulty;
    private final List<MoveData> movesHistory;
    private final Map<PieceColor, List<Piece>> piecesEaten;
    private boolean debug = false;

    public Game(Gamemode gamemode, Difficulty difficulty) {
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
        gameController = new GameController(board);

        gameController.setupBoard();
    }

    public boolean move(Tile from, Tile to) {
        boolean moveMade = gameController.movePiece(from, to, this::registerMove);

        if(debug) printBoard();

        return moveMade;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void registerMove(Tile from, Tile to) {
        movesHistory.add(new MoveData(board.pieceAt(from), from, to, board.removeFromTile(to)));
    }

    public List<MoveData> movesHistory() {
        return movesHistory;
    }

    public PieceColor colorTurn() {
        return gameController.colorTurn();
    }

    public void printBoard() {
        board.printBoard();
        System.out.println();
    }
}
