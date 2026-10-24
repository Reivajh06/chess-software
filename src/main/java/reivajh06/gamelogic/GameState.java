package reivajh06.gamelogic;

import reivajh06.FenParser;
import reivajh06.model.*;
import reivajh06.model.movement.MoveEvent;

import java.util.*;

public record GameState(
		Board board,
        Deque<MoveEvent> movesHistory,
		Map<Color, List<Piece>> eatenPieces,
        Color turnColor,
		Color checkedColor,
		GameStatus status,
		CastlingRights castlingRights,
		Tile enPassantDestination,
		int halfMoveClock,
		int fullMoves,
		Map<Position, Integer> positions
) {

    public static GameState startingState() {
		GameState initialState = new GameStateBuilder()
				.board(Board.startingBoard())
				.turnColor(Color.WHITE)
				.checkedColor(null)
				.eatenPieces(Map.of(Color.BLACK, new ArrayList<>(), Color.WHITE, new ArrayList<>()))
				.status(GameStatus.ACTIVE)
				.castlingRights(CastlingRights.allRights())
				.enPassantDestination(null)
				.halfMoveClock(0)
				.fullMoves(1)
				.build();

		initialState.positions.put(new Position(FenParser.gameStateToFen(initialState)), 1);

        return initialState;
    }

    public MoveEvent lastMove() {
        return movesHistory.isEmpty() ? null : movesHistory.peek();
    }
}
