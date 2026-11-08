package reivajh06.archchess.infrastructure.adapters.datalake.events;

import reivajh06.archchess.architecture.domain.model.game.GameStatus;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;

import java.util.UUID;

public class DeleteSessionEvent extends Event {

	private final String sessionId;
	private final Gamemode gamemode;
	private final GameStatus status;
	private final String fen;

	public DeleteSessionEvent(String username, UUID sessionId, Gamemode gamemode, GameStatus status, String fen) {
		super(username);

		this.sessionId = sessionId.toString();
		this.gamemode = gamemode;
		this.status = status;
		this.fen = fen;
	}
}
