package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.MoveEvent;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record GameState(
		Board board,
        Deque<MoveEvent> movesHistory,
		Map<PieceColor, List<Piece>> eatenPieces,
        PieceColor turnColor,
		PieceColor checkedColor,
		GameStatus status,
		List<Castling> whiteCastling,
		List<Castling> blackCastling,
		Tile enPassantDestination,
		int halfMoveClock,
		int fullMoves
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
                .halfMoveClock(0)
                .fullMoves(1)
                .build();
    }

    public String toFEN() {
        String whiteCastlingRights = whiteCastling.stream().filter(Objects::nonNull).map(Castling::firstLetter).collect(Collectors.joining());
        String blackCastlingRights = blackCastling.stream().filter(Objects::nonNull).map(c -> c.firstLetter().toLowerCase()).collect(Collectors.joining());

		String castlingRights = whiteCastlingRights + blackCastlingRights;

		if(castlingRights.isEmpty()) castlingRights = "-";

        return new StringBuilder()
                .append(board.toFEN())
                .append(" ")
                .append(turnColor.name().toLowerCase().charAt(0))
                .append(" ")
                .append(castlingRights)
                .append(" ")
                .append(enPassantDestination == null ? "-" : enPassantDestination.name().toLowerCase())
                .append(" ")
                .append(halfMoveClock)
                .append(" ")
                .append(fullMoves)
                .toString();
    }

    public MoveEvent lastMove() {
        return movesHistory.isEmpty() ? null : movesHistory.peek();
    }
}
