package reivajh06;

import reivajh06.api.ChessAPI;
import reivajh06.database.DataBaseConnection;
import reivajh06.database.Field;
import reivajh06.database.FieldConstraint;
import reivajh06.database.queries.CreateTableQuery;

import java.io.File;
import java.util.List;

public class Launcher {

	static void main(String[] args) {
		try(DataBaseConnection connection = new DataBaseConnection("temp/archdb.db")) {
			CreateTableQuery query = new CreateTableQuery()
					.name("USERS")
					.content(List.of(
							new Field("ID").ofInteger().autoincrement().constraint(FieldConstraint.PRIMARY_KEY),
							new Field("USERNAME").ofString().constraint(FieldConstraint.UNIQUE, FieldConstraint.NOT_NULL),
							new Field("HASH_PASSWORD").ofString().constraint(FieldConstraint.NOT_NULL))
					);

			connection.execute(query);

			setupApi(args);

		} catch(Exception e) {
			System.out.println("Error while initiating application");
		}
	}

	private static void setupApi(String[] args) {
		int port = args.length == 0 ? 5000 : Integer.parseInt(args[0]);
		File homeFolder = args.length == 0 ? new File("temp") : new File(args[1]);

		new ChessAPI(homeFolder, port).start();
	}
}
