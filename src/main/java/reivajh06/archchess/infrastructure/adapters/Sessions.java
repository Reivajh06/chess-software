package reivajh06.archchess.infrastructure.adapters;

import reivajh06.archchess.architecture.domain.model.game.GameSession;
import reivajh06.archchess.infrastructure.adapters.web.dtos.RequestMovePiece;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Sessions {

	private static final Map<UUID, GameSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();
	private static final Map<UUID, List<RequestMovePiece>> PLUGIN_SESSIONS_REQUESTS = new ConcurrentHashMap<>();

	public static void registerSession(GameSession session) {
		ACTIVE_SESSIONS.put(session.id(), session);
	}

	public static GameSession fetchSession(UUID sessionUUID) {
		return ACTIVE_SESSIONS.get(sessionUUID);
	}

	public static GameSession removeSession(UUID sessionUUID) {
		GameSession sessionRemoved = ACTIVE_SESSIONS.remove(sessionUUID);
		PLUGIN_SESSIONS_REQUESTS.remove(sessionUUID);

		return sessionRemoved;
	}
}
