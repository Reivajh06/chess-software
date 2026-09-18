import reivajh06.game_logic.Difficulty;
import reivajh06.game_logic.GameSession;
import reivajh06.game_logic.Gamemode;
import reivajh06.model.Tile;

void main() {
	GameSession gameSession = new GameSession(Gamemode.PVAI, Difficulty.NOT_CPU);
	gameSession.setDebug(true);

	System.out.println("Specify tiles with their name [Example: 'a2']");
	gameSession.printBoard();

	Scanner scanner = new Scanner(System.in);

	while(true) {
		System.out.println(String.format("==== %s TURN ====", gameSession.colorTurn()));

		System.out.print("From: ");
		String from = scanner.next(Pattern.compile("[abcdefgh][123456789]"));
		if(from.isBlank()) break;
		System.out.println();

		System.out.print("To: ");
		String to = scanner.next(Pattern.compile("[abcdefgh][123456789]"));
		if(to.isBlank()) break;
		System.out.println();

		if(!gameSession.move(new Tile(from.trim()), new Tile(to.trim()))) {
			System.out.println("Invalid move. Check the color of the turn and the validity of the move");
			continue;
		}
	}
}
