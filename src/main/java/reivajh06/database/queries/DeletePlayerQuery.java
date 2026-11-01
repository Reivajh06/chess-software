package reivajh06.database.queries;

import reivajh06.ShaEncoder;
import reivajh06.database.Player;

public class DeletePlayerQuery extends Query<Void> {


	public DeletePlayerQuery(Player player) {
		super("DELETE FROM PLAYERS WHERE USERNAME = ? AND HASH_PASSWORD = ?");

		params.add(player.username());
		params.add(ShaEncoder.encode(player.password()));
	}
}
