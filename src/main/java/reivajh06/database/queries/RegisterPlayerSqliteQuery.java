package reivajh06.database.queries;

import reivajh06.services.ShaEncoder;
import reivajh06.database.Player;

public class RegisterPlayerSqliteQuery extends JavalinQuery<Void> {

	public RegisterPlayerSqliteQuery(Player player) {
		super("INSERT INTO PLAYERS(USERNAME, HASH_PASSWORD) VALUES (?, ?)");

		params.add(player.username());
		params.add(ShaEncoder.encode(player.password()));
	}
}
