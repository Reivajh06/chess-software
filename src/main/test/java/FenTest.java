import reivajh06.FenParser;
import reivajh06.model.Board;

void main() {
	Board board = Board.startingBoard();
	System.out.println(FenParser.boardToFen(board));

	System.out.println(FenParser.boardToFen(board.copy()));
}
