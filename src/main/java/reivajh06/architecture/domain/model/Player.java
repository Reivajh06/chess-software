package reivajh06.architecture.domain.model;

public record Player(String username, String password) {

	public static Player guest(String sessionId) {
		return new Player("guest#" + sessionId, "");
	}
}
