package reivajh06.gamelogic;

import reivajh06.model.Board;
import reivajh06.model.Castling;
import reivajh06.model.PieceColor;
import reivajh06.model.Tile;
import reivajh06.model.movement.MoveData;

import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public record GameState(
		Board board,
        Deque<MoveData> movesHistory,
        PieceColor turnColor,
		PieceColor checkedColor,
		GameStatus status,
		List<Castling> whiteCastling,
		List<Castling> blackCastling,
		Tile enPassantDestination,
		int movesAfterPawn,
		int nTotalPlay
) {

    public static GameState startingState() {
        return new GameStateBuilder()
                .board(new Board())
                .turnColor(PieceColor.WHITE)
                .checkedColor(null)
                .status(GameStatus.ACTIVE)
                .whiteCastling(GameStateBuilder.CASTLING_ALL)
                .blackCastling(GameStateBuilder.CASTLING_ALL)
                .enPassantDestination(null)
                .movesAfterPawn(0)
                .nTotalPlay(1)
                .build();
    }

    public String toFEN() {
        String whiteCastlingRights = whiteCastling.stream().map(Castling::firstLetter).collect(Collectors.joining());
        String blackCastlingRights = blackCastling.stream().map(c -> c.firstLetter().toLowerCase()).collect(Collectors.joining());

        return new StringBuilder()
                .append(board.toFEN())
                .append(" ")
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

    public MoveData lastMove() {
        return movesHistory.isEmpty() ? null : movesHistory.peek();
    }
}
