package reivajh06.archchess.architecture.domain.model;

public record Player(String username, String password) {

	public static Player guest(String sessionId) {
		return new Player("guest#" + sessionId, "");
	}
}
