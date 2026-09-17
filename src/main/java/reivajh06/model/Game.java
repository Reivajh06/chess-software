package reivajh06.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    public PieceColor colorTurn = PieceColor.WHITE;

    private final GameController gameController;
    private Gamemode gamemode;
    private final Board board;
    private GameStatus status;
    private Difficulty difficulty;
    private final List<MoveData> movesHistory;
    private final Map<PieceColor, List<Piece>> piecesEaten;

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
        Piece p = board.pieceAt(from);

        if(colorTurn != p.color()) return false;

        return gameController.movePiece(from, to, this::registerMove);
    }

    public void registerMove(Tile from, Tile to) {
        movesHistory.add(new MoveData(board.pieceAt(from), from, to, board.removeFromTile(to)));
    }

    public List<MoveData> movesHistory() {
        return movesHistory;
    }

    public void debug() {
        board.debug();
        System.out.println();
    }
}
