package reivajh06.database.queries;

import reivajh06.database.Player;

public class GetPlayerQuery extends Query<Player> {

	public GetPlayerQuery(String username) {
		super(
				"SELECT USERNAME, HASH_PASSWORD FROM PLAYERS WHERE USERNAME = ?",
				r -> new Player(r.getString("USERNAME"), r.getString("HASH_PASSWORD"))
		);

		params.add(username);
	}
}
