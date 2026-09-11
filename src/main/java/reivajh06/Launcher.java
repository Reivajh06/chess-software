package reivajh06;

import reivajh06.model.Board;
import reivajh06.model.PieceColor;

public class Launcher {

	static void main(String[] args) {
		new ChessAPI().start();
		Board board = new Board();

		board.debug();
	}
}
