package software.ulpgc.gamelogic;

import software.ulpgc.model.PieceColor;
import software.ulpgc.model.Tile;

import java.util.List;

public record GameState(PieceColor turnColor, List<Castling> whiteCastling, List<Castling> blackCastling, Tile enPassantDestination, int movesAfterPawn, int nTotalPlay) {

    public static GameState startingState() {
        return new GameStateBuilder()
                .turnColor(PieceColor.WHITE)
                .whiteCastling(GameStateBuilder.CASTLING_ALL)
                .blackCastling(GameStateBuilder.CASTLING_ALL)
                .enPassantDestination(null)
                .build();
    }


}
