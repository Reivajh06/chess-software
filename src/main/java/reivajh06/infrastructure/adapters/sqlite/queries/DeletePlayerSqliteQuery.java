package reivajh06.infrastructure.adapters.sqlite.queries;

import reivajh06.infrastructure.adapters.sqlite.ShaEncoder;
import reivajh06.architecture.domain.model.Player;

public class DeletePlayerSqliteQuery extends JavalinQuery<Void> {


	public DeletePlayerSqliteQuery(Player player) {
		super("DELETE FROM PLAYERS WHERE USERNAME = ? AND HASH_PASSWORD = ?");

		params.add(player.username());
		params.add(ShaEncoder.encode(player.password()));
	}
}
