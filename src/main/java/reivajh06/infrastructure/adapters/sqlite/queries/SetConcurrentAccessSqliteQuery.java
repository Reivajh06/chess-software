package reivajh06.infrastructure.adapters.sqlite.queries;

public class SetConcurrentAccessSqliteQuery extends JavalinQuery<Void> {

	public SetConcurrentAccessSqliteQuery() {
		super("PRAGMA journal_mode=WAL");
	}
}
