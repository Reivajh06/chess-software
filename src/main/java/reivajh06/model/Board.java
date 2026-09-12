package reivajh06.model;

import reivajh06.model.pieces.*;

import java.util.HashMap;
import java.util.Map;

public class Board {

	//Unicode codification of columns, first is a, then b...
	private static final Map<Character, Integer> COLUMNSCODES = new HashMap<>(
			Map.of(
					'a', 0x61,
					'b', 0x62,
					'c' , 0x63,
					'd', 0x64,
					'e', 0x65,
					'f', 0x66,
					'g', 0x67,
					'h', 0x68
			)
	);

	private final Map<Tile, Piece> tiles;

	public Board() {
		tiles = new HashMap<>();

		allocatePieces(PieceColor.BLACK);
		fillMiddle();
		allocatePieces(PieceColor.WHITE);
	}

	public static int columnCode(char col) {
		return COLUMNSCODES.get(col);
	}

	public Piece move(Tile from, Tile to) {
		if(tiles.get(from) == null) return null;

		return tiles.replace(to, tiles.remove(from));
	}

	public Piece pieceAt(Tile tile) {
		return tiles.get(tile);
	}

	private void allocatePieces(PieceColor color) {
		int pawnRow = 2;
		int kingRow = 1;

		if(color == PieceColor.BLACK) {
			pawnRow = 7;
			kingRow = 8;
		}

		for(int i = 0; i < 8; i++) {						//Unicode based
			tiles.put(new Tile(Character.toString(COLUMNSCODES.get('a') + i), pawnRow), new Pawn(color));
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
				tiles.put(new Tile(Character.toString(COLUMNSCODES.get('a') + j), i), null);
			}
		}
	}

	public void debug() {
		for(int i = 8; i > 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(Character.toString(COLUMNSCODES.get('a') + j), i));
				row.append(p == null ? ".." : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
