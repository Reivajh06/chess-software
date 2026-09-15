package reivajh06.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    public PieceColor colorTurn = PieceColor.WHITE;

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
    }

    public boolean move(Square from, Square to) {
        Piece p = board.pieceAt(from);

        if(colorTurn != p.color()) return false;

        List<Square> validSquares = board.possiblePieceMoves(from);

        if(!validSquares.contains(to)) return false;

        Piece pieceEaten = board.move(from, to);

        if(pieceEaten != null) piecesEaten.get(pieceEaten.color()).add(pieceEaten);

        return true;
    }

    public void debug() {
        board.debug();
        System.out.println();
    }
}
