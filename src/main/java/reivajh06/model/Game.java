package reivajh06.model;

import reivajh06.model.pieces.Pawn;
import reivajh06.model.pieces.Piece;
import reivajh06.model.pieces.PieceColor;

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

    public boolean move(Tile from, Tile to) {
        Piece p = board.pieceAt(from);

        if(colorTurn != p.color()) return false;

        List<Tile> validTiles = board.possiblePieceMoves(from);

        if(!validTiles.contains(to)) return false;

        Piece pieceEaten = board.move(from, to);

        if(pieceEaten != null) piecesEaten.get(pieceEaten.color()).add(pieceEaten);

        return true;
    }

    public void debug() {
        board.debug();
        System.out.println();
    }
}
