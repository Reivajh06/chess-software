package reivajh06.database;

import reivajh06.database.queries.GetPlayerQuery;
import reivajh06.database.queries.Query;
import reivajh06.database.queries.RegisterPlayerQuery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

	private static final String JDBC_TAG = "jdbc:sqlite:";

	private final String databasePath;

	public Database(String databasePath) {
		this.databasePath = databasePath;
	}

	public <R> List<R> execute(Query<R> query) {
		try(Connection connection = DriverManager.getConnection(JDBC_TAG + databasePath)) {
			Statement statement = connection.createStatement();
			statement.execute(query.sql());
			ResultSet resultsPointer = statement.getResultSet();

			List<R> resultsList = new ArrayList<>();

			while(resultsPointer != null && resultsPointer.next()) {
				resultsList.add(query.map(resultsPointer));
			}

			statement.close();
			return resultsList;

		} catch(SQLException e) {
			throw new DatabaseException("Error while executing query: %s".formatted(query), e);
		}
	}

	public boolean playerExists(String username) {
		return !execute(new GetPlayerQuery(username)).isEmpty();
	}

	public void registerPlayer(Player player) {
		execute(new RegisterPlayerQuery(player));
	}
}
