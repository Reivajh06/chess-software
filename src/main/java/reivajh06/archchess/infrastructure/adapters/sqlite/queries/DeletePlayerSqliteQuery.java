package reivajh06.archchess.infrastructure.adapters.sqlite.queries;

import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.infrastructure.adapters.sqlite.ShaEncoder;

public class DeletePlayerSqliteQuery extends JavalinQuery<Void> {


	public DeletePlayerSqliteQuery(Player player) {
		super("DELETE FROM PLAYERS WHERE USERNAME = ? AND HASH_PASSWORD = ?");

		params.add(player.username());
		params.add(ShaEncoder.encode(player.password()));
	}
}
