package reivajh06.database.queries;

public class SetTimeoutSqliteQuery extends JavalinQuery<Void> {

	public SetTimeoutSqliteQuery() {
		super("PRAGMA busy_timeout=20000");
	}
}
