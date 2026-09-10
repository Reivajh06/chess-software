package reivajh06.model;

import java.util.Random;

public class Game {

    private Gamemode gamemode;
    private final Board board;
    private GameStatus status;

    public Game(Gamemode gamemode) {
        status = GameStatus.ACTIVE;
        this.gamemode = gamemode;

        board = new Board(new Random().nextInt(0, 2) == 0 ? PieceColor.BLACK : PieceColor.WHITE);
    }
}
