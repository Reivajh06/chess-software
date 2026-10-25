package reivajh06.chess.gamelogic;

import reivajh06.FenParser;
import reivajh06.chess.model.Board;
import reivajh06.chess.model.Color;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.Tile;
import reivajh06.chess.model.movement.MoveEvent;

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
