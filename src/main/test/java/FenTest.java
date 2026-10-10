import reivajh06.FenParser;
import reivajh06.model.Board;

void main() {
	Board board = new Board();
	System.out.println(FenParser.boardToFen(board));
}
