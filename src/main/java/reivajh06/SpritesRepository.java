package reivajh06;

import java.util.HashMap;
import java.util.Map;

public class SpritesRepository {

	public static final Map<String, String> SPRITES = new HashMap<>();

	static {
		SPRITES.put("WPA", "♙");
		SPRITES.put("WRO", "♖");
		SPRITES.put("WKI", "♔");
		SPRITES.put("WQU", "♕");
		SPRITES.put("WBI", "♗");
		SPRITES.put("WKN", "♘");
		SPRITES.put("BPA", "♟");
		SPRITES.put("BRO", "♜");
		SPRITES.put("BKI", "♚");
		SPRITES.put("BQU", "♛");
		SPRITES.put("BBI", "♝");
		SPRITES.put("BKN", "♞");
	}

	public static String sprite(String code) {
		return SPRITES.get(code);
	}
}
