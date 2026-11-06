package reivajh06.infrastructure.adapters.sqlite.queries;

public class CreatePlayerTableSqliteQuery extends JavalinQuery<Void> {

	public CreatePlayerTableSqliteQuery() {
		super(
				"""
				CREATE TABLE IF NOT EXISTS PLAYERS(
					ID INTEGER PRIMARY KEY,
					USERNAME VARCHAR NOT NULL,
					HASH_PASSWORD VARCHAR NOT NULL,
					UNIQUE(USERNAME, HASH_PASSWORD)
				);
				"""
		);
	}
}
