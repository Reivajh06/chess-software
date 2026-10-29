package reivajh06.database;

import reivajh06.database.queries.Query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection implements AutoCloseable {

	private static final String JDBC_TAG = "jdbc:sqlite:";

	private final Connection connection;

	public DataBaseConnection(String dbPath) throws SQLException {
		connection = DriverManager.getConnection(JDBC_TAG + dbPath);
	}

	public void execute(Query query) throws SQLException {
		connection.prepareStatement(query.body()).execute();
	}

	@Override
	public void close() throws Exception {
		connection.close();
	}
}
