package software.ulpgc.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public Piece removePieceIn(Tile t) {
		Piece p = tiles.get(t);
		replace(t, null);
		return p;
	}

	public Piece pieceAt(Tile tile) {
		return tiles.get(tile);
	}

	public void addPiece(Tile tile, Piece piece) {
		tiles.put(tile, piece);
	}

	public Piece replace(Tile tile, Piece piece) {
		return tiles.replace(tile, piece);
	}

	public Stream<Tile> asStream() {
		return tiles.keySet().stream();
	}

	public void printBoard() {
		System.out.println("  " + Arrays.stream(File.values()).map(File::name).collect(Collectors.joining("   ")));

		for(int i = 7; i >= 0; i--) {
			StringBuilder row = new StringBuilder();
			row.append(Rank.values()[i].number).append(" ");

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(File.values()[j], Rank.values()[i]));
				row.append(p == null ? ". " : p.toString());
				row.append("  ");
			}

			System.out.println(row);
		}
	}

	public Board copy() {
		Board board = new Board();

		for(Map.Entry<Tile, Piece> entry : tiles.entrySet()) {
			Tile tile = entry.getKey();
			Piece piece = entry.getValue();

			board.addPiece(new Tile(tile.file(), tile.rank()), piece == null ? null : piece.copy());
		}

		return board;
	}
}
