package reivajh06.database;

import reivajh06.ShaEncoder;
import reivajh06.database.queries.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

	private static final String JDBC_TAG = "jdbc:sqlite:";

	private final String databasePath;

	public Database(String databasePath) {
		this.databasePath = databasePath;
	}

	public void init() {
		execute(new SetConcurrentAccessQuery());
		execute(new SetTimeoutQuery());
		execute(new CreatePlayerTableQuery());
	}

	public <R> List<R> execute(Query<R> query) {
		try(Connection connection = DriverManager.getConnection(JDBC_TAG + databasePath)) {
			Statement statement;

			if(!query.params().isEmpty()) {
				statement = connection.prepareStatement(query.sql());

				for(int i = 0; i < query.params().size(); i++) {
					((PreparedStatement) statement).setObject(i + 1, query.params().get(i));
				}

				((PreparedStatement) statement).execute();
			} else {
				statement = connection.createStatement();
				statement.execute(query.sql());
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

	public boolean playerExists(Player player) {
		return !execute(new GetPlayerQuery(player.username(), ShaEncoder.encode(player.password()))).isEmpty();
	}

	public void registerPlayer(Player player) {
		execute(new RegisterPlayerQuery(player));
	}

	public void deletePlayer(Player player) {
		execute(new DeletePlayerQuery(player));
	}
}
