package reivajh06.infrastructure.adapters.sqlite.queries;

import reivajh06.infrastructure.adapters.sqlite.ShaEncoder;
import reivajh06.architecture.domain.model.Player;

public class RegisterPlayerSqliteQuery extends JavalinQuery<Void> {

	public RegisterPlayerSqliteQuery(Player player) {
		super("INSERT INTO PLAYERS(USERNAME, HASH_PASSWORD) VALUES (?, ?)");

		params.add(player.username());
		params.add(ShaEncoder.encode(player.password()));
	}
}
