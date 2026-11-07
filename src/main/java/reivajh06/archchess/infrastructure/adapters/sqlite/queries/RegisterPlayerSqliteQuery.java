package reivajh06.archchess.infrastructure.adapters.sqlite.queries;

import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.infrastructure.adapters.sqlite.ShaEncoder;

public class RegisterPlayerSqliteQuery extends JavalinQuery<Void> {

	public RegisterPlayerSqliteQuery(Player player) {
		super("INSERT INTO PLAYERS(USERNAME, HASH_PASSWORD) VALUES (?, ?)");

		params.add(player.username());
		params.add(ShaEncoder.encode(player.password()));
	}
}
