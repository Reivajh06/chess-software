package reivajh06.architecture.ports.database;

import reivajh06.architecture.domain.model.Player;

public interface UserDatabaseClient {

    void init();

    boolean playerExists(Player player);

    boolean usernameExists(String username);

    void registerPlayer(Player player);

    void deletePlayer(Player player);
}
