package reivajh06;

import reivajh06.api.ChessAPI;
import reivajh06.model.Board;

public class Launcher {

	static void main(String[] args) {
		new ChessAPI().start();
		Board board = new Board();

		board.debug();
	}
}
