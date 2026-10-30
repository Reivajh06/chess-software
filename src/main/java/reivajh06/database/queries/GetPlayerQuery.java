package reivajh06.database.queries;

import reivajh06.database.Player;

public class GetPlayerQuery extends Query<Player> {

	public GetPlayerQuery(String username) {
		super(
				"SELECT USERNAME, HASH_PASSWORD FROM PLAYERS WHERE USERNAME = '%s'".formatted(username),
				r -> new Player(r.getString("USERNAME"), r.getString(r.getString("HASH_PASSWORD")))
		);
	}
}
