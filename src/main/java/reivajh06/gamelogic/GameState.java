package reivajh06.gamelogic;

import reivajh06.FenParser;
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
                .board(Board.startingBoard())
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

    public MoveEvent lastMove() {
        return movesHistory.isEmpty() ? null : movesHistory.peek();
    }
}
