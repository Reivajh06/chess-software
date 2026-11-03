package reivajh06.database;

public record Player(String username, String password) {

	public static Player guest(String ip) {
		return new Player(ip, "");
	}
}
