package reivajh06.model;

import reivajh06.model.pieces.*;

import java.util.HashMap;
import java.util.Map;

public class Board {

	//Tile -> null if does not contain any piece. Could be changed to a Tiles enum
	private final Map<Tile, Piece> tiles;

	public Board() {
		tiles = new HashMap<>();

		allocatePieces(PieceColor.BLACK);
		fillMiddle();
		allocatePieces(PieceColor.WHITE);
	}

	public Piece removeFromTile(Tile tile) {
		return tiles.replace(tile, null);
	}

	private void allocatePieces(PieceColor color) {
		int pawnRow = 2;
		int kingRow = 1;

		if(color == PieceColor.BLACK) {
			pawnRow = 7;
			kingRow = 8;
		}

		for(int i = 0; i < 8; i++) {						//Unicode based
			tiles.put(new Tile(Character.toString(0x61 + i), pawnRow), new Pawn(color));
		}

		tiles.put(new Tile("a", kingRow), new Rook(color));
		tiles.put(new Tile("b", kingRow), new Knight(color));
		tiles.put(new Tile("c", kingRow), new Bishop(color));
		tiles.put(new Tile("d", kingRow), new Queen(color));
		tiles.put(new Tile("e", kingRow), new King(color));
		tiles.put(new Tile("f", kingRow), new Bishop(color));
		tiles.put(new Tile("g", kingRow), new Knight(color));
		tiles.put(new Tile("h", kingRow), new Rook(color));
	}

	private void fillMiddle() {
		for(int i = 3; i <= 6; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(new Tile(Character.toString(0x61 + j), i), null);
			}
		}
	}

	public void debug() {
		for(int i = 8; i > 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(Character.toString(0x61 + j), i));
				row.append(p == null ? "null" : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
