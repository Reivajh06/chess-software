package reivajh06.archchess.infrastructure.adapters.datalake.events;

import reivajh06.archchess.architecture.domain.model.game.Difficulty;
import reivajh06.archchess.architecture.domain.model.game.Gamemode;

import java.util.UUID;

public class CreateSessionEvent extends Event {

	private final String sessionId;
	private final Gamemode gamemode;
	private final Difficulty difficulty;

	public CreateSessionEvent(String username, UUID sessionId, Gamemode gamemode, Difficulty difficulty) {
		super(username);
		this.sessionId = sessionId.toString();
		this.gamemode = gamemode;
		this.difficulty = difficulty;
	}

	public String sessionId() {
		return sessionId;
	}

	public Gamemode gamemode() {
		return gamemode;
	}

	public Difficulty difficulty() {
		return difficulty;
	}
}
