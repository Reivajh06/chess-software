package reivajh06.archchess.infrastructure.adapters.sqlite;

public class DatabaseException extends RuntimeException {

	public DatabaseException(String message, Exception cause) {
		super(message, cause);
	}
}
