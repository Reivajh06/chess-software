package reivajh06.database.queries;

import reivajh06.services.ShaEncoder;
import reivajh06.database.Player;

public class DeletePlayerSqliteQuery extends JavalinQuery<Void> {


	public DeletePlayerSqliteQuery(Player player) {
		super("DELETE FROM PLAYERS WHERE USERNAME = ? AND HASH_PASSWORD = ?");

		params.add(player.username());
		params.add(ShaEncoder.encode(player.password()));
	}
}
