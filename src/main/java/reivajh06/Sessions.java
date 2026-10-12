package reivajh06;

import reivajh06.gamelogic.GameSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Sessions {

	public static final Map<UUID, GameSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

	public static boolean registerSession(GameSession session) {
		if(fetchSession(session.id()) != null) return false;

		ACTIVE_SESSIONS.put(session.id(), session);

		return true;
	}

	public static GameSession fetchSession(UUID sessionUUID) {
		return ACTIVE_SESSIONS.get(sessionUUID);
	}

	public static GameSession removeSession(UUID sessionUUID) {
		return ACTIVE_SESSIONS.remove(sessionUUID);
	}
}
