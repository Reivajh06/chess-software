package reivajh06.api.dtos;

import reivajh06.chess.gamelogic.GameStatus;
import reivajh06.chess.model.Color;
import reivajh06.chess.model.Piece;
import reivajh06.chess.model.movement.MoveEvent;

import java.util.Deque;
import java.util.List;
import java.util.Map;

public record ReadStateDto(
		GameStatus status,
		boolean waitingForOpponent,
		Color turnColor,
		Color checkedColor,
		String fen,
		Map<Color, List<Piece>> eatenPieces,
		Deque<MoveEvent> movesHistory
) {
}
