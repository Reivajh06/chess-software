package reivajh06.database.queries;

public class CreatePlayerTableQuery extends Query<Void> {

	public CreatePlayerTableQuery() {
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
