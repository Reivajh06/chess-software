package reivajh06.archchess.infrastructure.adapters.sqlite.queries;

public class SetTimeoutSqliteQuery extends JavalinQuery<Void> {

	public SetTimeoutSqliteQuery() {
		super("PRAGMA busy_timeout=20000");
	}
}
