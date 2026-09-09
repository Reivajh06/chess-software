package reivajh06.model;

import java.util.List;
import java.util.Map;

public class Game {

	private final int id;
	private GameMode gameMode;
	private GameStatus status;

	//Should be either white or black
	private PieceColor turnColor;

	private PieceColor winnerColor;

	//Defined as square - pieceCode which defines where each piece is located
	private Map<String, String> board;

	private List<MoveData> movesHistory;
	private List<String> capturedByWhite;
	private List<String> capturedByBlack;

	private Map<String, Integer> score;
	private Map<String, String> lastMove;

	private boolean checkSquare;
}
