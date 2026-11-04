package reivajh06.datalake.events;

import reivajh06.chess.gamelogic.Gamemode;

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
