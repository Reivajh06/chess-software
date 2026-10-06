package reivajh06.gamelogic;

import reivajh06.model.Castling;
import reivajh06.model.PieceColor;
import reivajh06.model.Tile;

import java.util.List;

public class GameStateBuilder {

    public static final List<Castling> CASTLING_ALL = List.of(Castling.KINGSIDE, Castling.QUEENSIDE);

    private PieceColor turnColor;
    private List<Castling> whiteCastling;
    private List<Castling> blackCastling;
    private Tile enPassantDestination;
    private int movesAfterPawn;
    private int nTotalPlay;

    public GameStateBuilder turnColor(PieceColor turnColor) {
        this.turnColor = turnColor;
        return this;
    }

    public GameStateBuilder whiteCastling(List<Castling> whiteCastling) {
        this.whiteCastling = whiteCastling;
        return this;
    }

    public GameStateBuilder blackCastling(List<Castling> blackCastling) {
        this.blackCastling = blackCastling;
        return this;
    }

    public GameStateBuilder enPassantDestination(Tile enPassantDestination) {
        this.enPassantDestination = enPassantDestination;
        return this;
    }

    public GameStateBuilder movesAfterPawn(int movesAfterPawn) {
        this.movesAfterPawn = movesAfterPawn;
        return this;
    }

    public GameStateBuilder nTotalPlay(int nTotalPlay) {
        this.nTotalPlay = nTotalPlay;
        return this;
    }

    public GameState build() {
        return new GameState(
                turnColor,
                whiteCastling,
                blackCastling,
                enPassantDestination,
                movesAfterPawn,
                nTotalPlay == 0 ? 1 : nTotalPlay
        );
    }
}
