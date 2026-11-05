package reivajh06.database.queries;

public class SetConcurrentAccessSqliteQuery extends JavalinQuery<Void> {

	public SetConcurrentAccessSqliteQuery() {
		super("PRAGMA journal_mode=WAL");
	}
}
