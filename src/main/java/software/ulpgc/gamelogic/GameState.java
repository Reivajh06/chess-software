package software.ulpgc.gamelogic;

import software.ulpgc.model.PieceColor;
import software.ulpgc.model.Tile;

import java.util.List;
import java.util.stream.Collectors;

public record GameState(PieceColor turnColor, List<Castling> whiteCastling, List<Castling> blackCastling, Tile enPassantDestination, int movesAfterPawn, int nTotalPlay) {

    public static GameState startingState() {
        return new GameStateBuilder()
                .turnColor(PieceColor.WHITE)
                .whiteCastling(GameStateBuilder.CASTLING_ALL)
                .blackCastling(GameStateBuilder.CASTLING_ALL)
                .enPassantDestination(null)
                .build();
    }

    public String toFEN() {
        String whiteCastlingRights = whiteCastling.stream().map(Castling::firstLetter).collect(Collectors.joining());
        String blackCastlingRights = blackCastling.stream().map(c -> c.firstLetter().toLowerCase()).collect(Collectors.joining());

        return new StringBuilder()
                .append(turnColor.name().toLowerCase().charAt(0))
                .append(" ")
                .append(whiteCastlingRights)
                .append(blackCastlingRights)
                .append(" ")
                .append(enPassantDestination == null ? "-" : enPassantDestination.name().toLowerCase())
                .append(" ")
                .append(movesAfterPawn)
                .append(" ")
                .append(nTotalPlay)
                .toString();
    }
}
