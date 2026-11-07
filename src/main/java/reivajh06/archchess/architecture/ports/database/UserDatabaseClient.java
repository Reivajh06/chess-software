package reivajh06.archchess.architecture.ports.database;

import reivajh06.archchess.architecture.domain.model.Player;

public interface UserDatabaseClient {

    void init();

    boolean playerExists(Player player);

    boolean usernameExists(String username);

    void registerPlayer(Player player);

    void deletePlayer(Player player);
}
