package reivajh06.archchess.infrastructure.adapters.sqlite.queries;

import reivajh06.archchess.architecture.domain.model.Player;

public class GetPlayerSqliteQuery extends JavalinQuery<Player> {

	public GetPlayerSqliteQuery(String username, String hashPassword) {
		super(
				"SELECT USERNAME, HASH_PASSWORD FROM PLAYERS WHERE USERNAME = ? AND HASH_PASSWORD = ?",
				r -> new Player(r.getString("USERNAME"), r.getString("HASH_PASSWORD"))
		);

		params.add(username);
		params.add(hashPassword);
	}

	public GetPlayerSqliteQuery(String username) {
		super(
				"SELECT USERNAME, HASH_PASSWORD FROM PLAYERS WHERE USERNAME = ?",
				r -> new Player(r.getString("USERNAME"), r.getString("HASH_PASSWORD"))
		);

		params.add(username);
	}
}
