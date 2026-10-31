package reivajh06.database.queries;

public class SetConcurrentAccessQuery extends Query<Void> {

	public SetConcurrentAccessQuery() {
		super("PRAGMA journal_mode=WAL");
	}
}
