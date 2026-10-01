package software.ulpgc;

import java.util.HashMap;
import java.util.Map;

public class SpritesRepository {

	public static final Map<String, String> SPRITES = new HashMap<>();

	static {
		SPRITES.put("WP", "♙");
		SPRITES.put("WR", "♖");
		SPRITES.put("WK", "♔");
		SPRITES.put("WQ", "♕");
		SPRITES.put("WB", "♗");
		SPRITES.put("WN", "♘");
		SPRITES.put("BP", "♟");
		SPRITES.put("BR", "♜");
		SPRITES.put("BK", "♚");
		SPRITES.put("BQ", "♛");
		SPRITES.put("BB", "♝");
		SPRITES.put("BN", "♞");
	}

	public static String sprite(String code) {
		return SPRITES.get(code);
	}
}
