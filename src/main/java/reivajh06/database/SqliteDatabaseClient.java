package reivajh06.database;

import reivajh06.services.ShaEncoder;
import reivajh06.database.queries.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteDatabaseClient implements UserDatabaseClient {

	private static final String JDBC_TAG = "jdbc:sqlite:";

	private final String databasePath;

	public SqliteDatabaseClient(String databasePath) {
		this.databasePath = databasePath;
	}

	@Override
	public void init() {
		execute(new SetConcurrentAccessSqliteQuery());
		execute(new SetTimeoutSqliteQuery());
		execute(new CreatePlayerTableSqliteQuery());
	}

	@Override
	public <R> List<R> execute(Query<R> query) {
		try(Connection connection = DriverManager.getConnection(JDBC_TAG + databasePath)) {
			Statement statement;

			if(!query.params().isEmpty()) {
				statement = connection.prepareStatement(query.body());

				for(int i = 0; i < query.params().size(); i++) {
					((PreparedStatement) statement).setObject(i + 1, query.params().get(i));
				}

				((PreparedStatement) statement).execute();
			} else {
				statement = connection.createStatement();
				statement.execute(query.body());
			}

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

	@Override
	public boolean playerExists(Player player) {
		return !execute(new GetPlayerSqliteQuery(player.username(), ShaEncoder.encode(player.password()))).isEmpty();
	}

	@Override
	public boolean usernameExists(String username) {
		return !execute(new GetPlayerSqliteQuery(username)).isEmpty();
	}

	@Override
	public void registerPlayer(Player player) {
		execute(new RegisterPlayerSqliteQuery(player));
	}

	@Override
	public void deletePlayer(Player player) {
		execute(new DeletePlayerSqliteQuery(player));
	}
}
