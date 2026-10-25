package reivajh06;

import reivajh06.chess.gamelogic.GameSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Sessions {

	private static final Map<UUID, GameSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

	public static void registerSession(GameSession session) {
		ACTIVE_SESSIONS.put(session.id(), session);
	}

	public static GameSession fetchSession(UUID sessionUUID) {
		return ACTIVE_SESSIONS.get(sessionUUID);
	}

	public static GameSession removeSession(UUID sessionUUID) {
		return ACTIVE_SESSIONS.remove(sessionUUID);
	}
}
