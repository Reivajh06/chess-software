package reivajh06.model;

import java.util.HashMap;
import java.util.Map;

public class Board {

	//Tile -> null if does not contain any piece. Could be changed to a Tiles enum
	private final Map<String, Piece> tiles;

	public Board(PieceColor userColor) {
		tiles = new HashMap<>();

		allocatePieces(userColor);
	}

	public Piece removeFromTile(String tile) {
		return tiles.replace(tile, null);
	}

	private void allocatePieces(PieceColor userColor) {
		allocatePieces(true, userColor == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);
		fillMiddle();
		allocatePieces(false, userColor);
	}

	private void allocatePieces(boolean placedFront, PieceColor color) {
		if(placedFront) {
			for(int i = 0; i < 8; i++) {						//Unicode based
				tiles.put(Character.toString(0x61 + i) + 7, new Piece(PieceType.PAWN, color));
			}

			tiles.put("a8", new Piece(PieceType.ROOK, color));
			tiles.put("b8", new Piece(PieceType.HORSE, color));
			tiles.put("c8", new Piece(PieceType.BISHOP, color));
			tiles.put("d8", new Piece(PieceType.QUEEN, color));
			tiles.put("e8", new Piece(PieceType.KING, color));
			tiles.put("f8", new Piece(PieceType.BISHOP, color));
			tiles.put("g8", new Piece(PieceType.HORSE, color));
			tiles.put("h8", new Piece(PieceType.ROOK, color));

			return;
		}

		for(int i = 0; i < 8; i++) {						//Unicode based
			tiles.put(Character.toString(0x61 + i) + 2, new Piece(PieceType.PAWN, color));
		}

		tiles.put("a1", new Piece(PieceType.ROOK, color));
		tiles.put("b1", new Piece(PieceType.HORSE, color));
		tiles.put("c1", new Piece(PieceType.BISHOP, color));
		tiles.put("d1", new Piece(PieceType.QUEEN, color));
		tiles.put("e1", new Piece(PieceType.KING, color));
		tiles.put("f1", new Piece(PieceType.BISHOP, color));
		tiles.put("g1", new Piece(PieceType.HORSE, color));
		tiles.put("h1", new Piece(PieceType.ROOK, color));
	}

	private void fillMiddle() {
		for(int i = 3; i <= 6; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(Character.toString(0x61 + j) + i, null);
			}
		}
	}

	public void debug() {
		for(int i = 1; i <= 8; i++) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(Character.toString(0x61 + j) + i);
				row.append(p == null ? "null" : p.type());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
