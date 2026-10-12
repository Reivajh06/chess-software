package reivajh06.gamelogic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sessions {

	public static final Map<UUID, GameSession> ACTIVE_SESSIONS = new HashMap<>();

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
