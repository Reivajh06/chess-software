package reivajh06.api.adapters;

import reivajh06.FenParser;
import reivajh06.Json;
import reivajh06.api.dto.ReadStateDto;
import reivajh06.api.ports.OutputSerializer;
import reivajh06.chess.gamelogic.GameSession;

public class ReadStateSerializer implements OutputSerializer<GameSession> {

	@Override
	public String serialize(GameSession session) {
		ReadStateDto readStateDto = new ReadStateDto(
				session.gameState().status(),
				session.whiteId() == null || session.blackId() == null,
				session.gameState().turnColor(),
				session.gameState().checkedColor(),
				FenParser.gameStateToFen(session.gameState()),
				session.gameState().eatenPieces(),
				session.gameState().movesHistory()
		);

		return Json.toJson(readStateDto);
	}
}
