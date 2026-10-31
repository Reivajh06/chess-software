package reivajh06.database.queries;

public class SetTimeoutQuery extends Query<Void> {

	public SetTimeoutQuery() {
		super("PRAGMA busy_timeout=20000");
	}
}
