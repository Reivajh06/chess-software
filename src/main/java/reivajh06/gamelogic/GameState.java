package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.MoveEvent;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public record GameState(
		Board board,
        Deque<MoveEvent> movesHistory,
		Map<Color, List<Piece>> eatenPieces,
        Color turnColor,
		Color checkedColor,
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
                .turnColor(Color.WHITE)
                .checkedColor(null)
				.eatenPieces(Map.of(Color.BLACK, new ArrayList<>(), Color.WHITE, new ArrayList<>()))
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
