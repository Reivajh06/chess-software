package reivajh06.model;

import java.util.HashMap;
import java.util.Map;

public class Board {

	private final Map<Tile, Piece> tiles;

	public Board() {
		tiles = new HashMap<>();

		File[] files = File.values();
		Rank[] ranks = Rank.values();

		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(new Tile(files[j], ranks[i]), null);
			}
		}
	}

	public void move(Tile from, Tile to) {
		tiles.put(to, pieceAt(from));
	}

	public Piece removeFromTile(Tile t) {
		return tiles.remove(t);
	}

	public Piece pieceAt(Tile tile) {
		return tiles.get(tile);
	}

	public boolean hasPiece(Tile tile) {
		return pieceAt(tile) != null;
	}

	public void addPiece(Tile tile, Piece piece) {
		tiles.put(tile, piece);
	}

	public Piece replace(Tile tile, Piece piece) {
		return tiles.replace(tile, piece);
	}

	public void debug() {
		for(int i = 7; i >= 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(File.values()[j], Rank.values()[i]));
				row.append(p == null ? "..." : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
