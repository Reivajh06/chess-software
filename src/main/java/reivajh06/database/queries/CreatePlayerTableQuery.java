package reivajh06.database.queries;

public class CreatePlayerTableQuery extends Query<Void> {

	public CreatePlayerTableQuery() {
		super(
				"""
				PRAGMA journal_mode=WAL;
				PRAGMA busy_timeout=20000;
				
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
