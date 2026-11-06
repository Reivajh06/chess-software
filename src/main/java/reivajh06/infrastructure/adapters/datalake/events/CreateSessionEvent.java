package reivajh06.infrastructure.adapters.datalake.events;

import reivajh06.architecture.domain.model.game.Gamemode;

import java.util.UUID;

public class CreateSessionEvent extends Event {

	private final String sessionId;
	private final Gamemode gamemode;

	public CreateSessionEvent(String username, UUID sessionId, Gamemode gamemode) {
		super(username);
		this.sessionId = sessionId.toString();
		this.gamemode = gamemode;
	}

	public String sessionId() {
		return sessionId;
	}

	public Gamemode gamemode() {
		return gamemode;
	}
}
