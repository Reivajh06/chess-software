import reivajh06.model.Difficulty;
import reivajh06.model.Game;
import reivajh06.model.Gamemode;
import reivajh06.model.Tile;

public class GameTest {

	static void main(String[] args) {
		Game game = new Game(Gamemode.PVAI, Difficulty.NOT_CPU);
		game.debug();

		game.move(new Tile("a2"), new Tile("a3"));
		game.debug();
	}

	private static void move(Game game, String from, String to) {
		System.out.println(from + " -> " + to);
		game.move(new Tile(from), new Tile(to));
		game.debug();
	}
}