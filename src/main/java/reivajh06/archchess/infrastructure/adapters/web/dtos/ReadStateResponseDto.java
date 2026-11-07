package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.Color;
import reivajh06.archchess.architecture.domain.model.Piece;
import reivajh06.archchess.architecture.domain.model.game.GameStatus;
import reivajh06.archchess.architecture.domain.model.movement.MoveEvent;

import java.util.Deque;
import java.util.List;
import java.util.Map;

public record ReadStateResponseDto(
		GameStatus status,
		boolean waitingForOpponent,
		Color turnColor,
		Color checkedColor,
		String fen,
		Map<Color, List<Piece>> eatenPieces,
		Deque<MoveEvent> movesHistory
) {
}
