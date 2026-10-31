package reivajh06.database.queries;

import reivajh06.database.Player;

public class RegisterPlayerQuery extends Query<Void> {

	public RegisterPlayerQuery(Player player) {
		super("INSERT INTO PLAYERS(USERNAME, HASH_PASSWORD) VALUES (?, ?)");

		params.add(player.username());
		params.add(player.hashPassword());
	}
}
