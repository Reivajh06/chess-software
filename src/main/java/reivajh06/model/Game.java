package reivajh06.model;

import reivajh06.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private Gamemode gamemode;
    private final Board board;
    private GameStatus status;
    private Difficulty difficulty;
    private final List<MoveData> movesHistory;
    private final List<Piece> whiteEaten;
    private final List<Piece> blackEaten;

    public Game(Gamemode gamemode, Difficulty difficulty) {
        this.difficulty = difficulty;
        this.gamemode = gamemode;

        status = GameStatus.ACTIVE;
        movesHistory = new ArrayList<>();
        whiteEaten = new ArrayList<>();
        blackEaten = new ArrayList<>();
        board = new Board();
    }
}
