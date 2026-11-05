package reivajh06.database;

import reivajh06.database.queries.Query;

import java.util.List;

public interface UserDatabaseClient {

    void init();

    <R> List<R> execute(Query<R> query);

    boolean playerExists(Player player);

    void registerPlayer(Player player);

    void deletePlayer(Player player);
}
