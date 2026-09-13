import reivajh06.model.Difficulty;
import reivajh06.model.Game;
import reivajh06.model.Gamemode;
import reivajh06.model.Tile;

public class MovesetTest {

	static void main() {
		Game game = new Game(Gamemode.PVAI, Difficulty.NOT_CPU);

		game.move(new Tile("a2"), new Tile("a4"));
		game.debug();
	}
}
